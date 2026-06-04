package com.vitalcheck.data.remote.fitbit

import android.content.SharedPreferences
import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.vitalcheck.BuildConfig
import com.vitalcheck.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitbitAuthManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val moshi: Moshi
) {
    private val httpClient = OkHttpClient()

    fun hasValidToken(): Boolean {
        val token = prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
        val expiry = prefs.getLong(Constants.KEY_TOKEN_EXPIRY, 0)
        return token != null && System.currentTimeMillis() < expiry
    }

    fun getAccessToken(): String? = prefs.getString(Constants.KEY_ACCESS_TOKEN, null)

    fun buildAuthUrl(): String {
        val codeVerifier = generateCodeVerifier()
        prefs.edit().putString(Constants.KEY_CODE_VERIFIER, codeVerifier).apply()

        val codeChallenge = generateCodeChallenge(codeVerifier)

        return Uri.parse(Constants.FITBIT_AUTH_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .appendQueryParameter("scope", "activity heartrate sleep")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("redirect_uri", BuildConfig.FITBIT_REDIRECT_URI)
            .build()
            .toString()
    }

    fun handleAuthCode(code: String) {
        runBlocking(Dispatchers.IO) {
            exchangeCodeForToken(code)
        }
    }

    suspend fun refreshTokenIfNeeded(): Boolean {
        val expiry = prefs.getLong(Constants.KEY_TOKEN_EXPIRY, 0)
        if (System.currentTimeMillis() < expiry - 60_000) return true // still valid with 1min buffer

        val refreshToken = prefs.getString(Constants.KEY_REFRESH_TOKEN, null) ?: return false
        return refreshToken(refreshToken)
    }

    private fun basicAuthHeader(): String {
        val credentials = "${BuildConfig.FITBIT_CLIENT_ID}:${BuildConfig.FITBIT_CLIENT_SECRET}"
        return "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray(Charsets.UTF_8))
    }

    private suspend fun exchangeCodeForToken(code: String) {
        val codeVerifier = prefs.getString(Constants.KEY_CODE_VERIFIER, null) ?: return

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .add("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .add("redirect_uri", BuildConfig.FITBIT_REDIRECT_URI)
            .build()

        val request = Request.Builder()
            .url(Constants.FITBIT_TOKEN_URL)
            .header("Authorization", basicAuthHeader())
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return
            val tokenResponse = moshi.adapter(TokenResponse::class.java).fromJson(responseBody) ?: return
            saveTokens(tokenResponse)
        }
    }

    private suspend fun refreshToken(refreshToken: String): Boolean {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .build()

        val request = Request.Builder()
            .url(Constants.FITBIT_TOKEN_URL)
            .header("Authorization", basicAuthHeader())
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return false
            val tokenResponse = moshi.adapter(TokenResponse::class.java).fromJson(responseBody) ?: return false
            saveTokens(tokenResponse)
            return true
        }
        return false
    }

    private fun saveTokens(tokenResponse: TokenResponse) {
        prefs.edit()
            .putString(Constants.KEY_ACCESS_TOKEN, tokenResponse.accessToken)
            .putString(Constants.KEY_REFRESH_TOKEN, tokenResponse.refreshToken)
            .putLong(Constants.KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (tokenResponse.expiresIn * 1000L))
            .apply()
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    @JsonClass(generateAdapter = true)
    data class TokenResponse(
        @Json(name = "access_token") val accessToken: String,
        @Json(name = "refresh_token") val refreshToken: String,
        @Json(name = "expires_in") val expiresIn: Int,
        @Json(name = "token_type") val tokenType: String
    )
}
