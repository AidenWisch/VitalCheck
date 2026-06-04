package com.vitalcheck.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vitalcheck.BuildConfig
import com.vitalcheck.data.remote.anthropic.AnthropicApiService
import com.vitalcheck.data.remote.fitbit.FitbitApiService
import com.vitalcheck.data.remote.fitbit.FitbitAuthInterceptor
import com.vitalcheck.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    @Named("fitbit")
    fun provideFitbitClient(
        authInterceptor: FitbitAuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    @Named("anthropic")
    fun provideAnthropicClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                .header("anthropic-version", Constants.ANTHROPIC_VERSION)
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideFitbitApiService(
        @Named("fitbit") client: OkHttpClient,
        moshi: Moshi
    ): FitbitApiService = Retrofit.Builder()
        .baseUrl(Constants.FITBIT_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FitbitApiService::class.java)

    @Provides
    @Singleton
    fun provideAnthropicApiService(
        @Named("anthropic") client: OkHttpClient,
        moshi: Moshi
    ): AnthropicApiService = Retrofit.Builder()
        .baseUrl(Constants.ANTHROPIC_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AnthropicApiService::class.java)
}
