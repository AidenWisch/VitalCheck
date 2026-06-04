package com.vitalcheck.data.remote.fitbit

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitbitAuthInterceptor @Inject constructor(
    private val authManager: FitbitAuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Ensure token is fresh
        runBlocking { authManager.refreshTokenIfNeeded() }

        val token = authManager.getAccessToken()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(request)

        // Handle 401 by refreshing and retrying once
        if (response.code == 401) {
            response.close()
            val refreshed = runBlocking { authManager.refreshTokenIfNeeded() }
            if (refreshed) {
                val newToken = authManager.getAccessToken()
                val retryRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }
}
