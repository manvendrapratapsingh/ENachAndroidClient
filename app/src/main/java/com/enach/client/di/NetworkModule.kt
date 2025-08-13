package com.enach.client.di

import com.enach.client.BuildConfig
import com.enach.client.data.api.ENachApiService
import com.enach.client.data.repository.AuthTokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ResponseLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            // Enhanced logging with custom tag
            android.util.Log.d("API_RAW_RESPONSE", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(tokenManager: AuthTokenManager): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            // Add auth token if available and not already present
            if (!original.header("Authorization").isNullOrEmpty()) {
                // Authorization header already present, don't override
            } else {
                tokenManager.getToken()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
            
            // Add other headers
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Content-Type", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
    }
    
    @Provides
    @Singleton
    @ResponseLoggingInterceptor
    fun provideResponseLoggingInterceptor(gson: Gson): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            
            // Log raw JSON response with pretty formatting
            if (BuildConfig.DEBUG) {
                val responseBody = response.body
                val source = responseBody?.source()
                source?.request(Long.MAX_VALUE) // Buffer the entire body
                val buffer = source?.buffer
                
                val responseBodyString = buffer?.clone()?.readUtf8()
                
                android.util.Log.d("API_REQUEST", "URL: ${response.request.url}")
                android.util.Log.d("API_REQUEST", "Method: ${response.request.method}")
                android.util.Log.d("API_RESPONSE", "Status Code: ${response.code}")
                android.util.Log.d("API_RESPONSE", "Headers: ${response.headers}")
                
                responseBodyString?.let { jsonString ->
                    try {
                        // Pretty print JSON
                        val jsonElement = gson.fromJson(jsonString, com.google.gson.JsonElement::class.java)
                        val prettyJson = gson.newBuilder().setPrettyPrinting().create().toJson(jsonElement)
                        android.util.Log.d("API_RAW_JSON", "Raw Response JSON:\n$prettyJson")
                        
                        // Also log the plain JSON for copying
                        android.util.Log.d("API_PLAIN_JSON", jsonString)
                    } catch (e: Exception) {
                        android.util.Log.d("API_RAW_JSON", "Raw Response: $jsonString")
                    }
                }
            }
            
            response
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @AuthInterceptor authInterceptor: Interceptor,
        @ResponseLoggingInterceptor responseLoggingInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(responseLoggingInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideENachApiService(retrofit: Retrofit): ENachApiService {
        return retrofit.create(ENachApiService::class.java)
    }
}
