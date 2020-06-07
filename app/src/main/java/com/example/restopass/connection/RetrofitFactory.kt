package com.example.restopass.connection

import com.example.restopass.connection.interceptor.AuthInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {

    fun <T> createClient(baseUrl: String, clazz: Class<T>, needAuth: Boolean = true): T {
        val noAuthClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .build()


        return Retrofit.Builder()
            .baseUrl(baseUrl).apply {
                if (needAuth) client(okHttpClient)
                else client(noAuthClient)
            }
            .addConverterFactory(JacksonConverterFactory.create(ObjectMapperProvider.mapper))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(clazz)
    }
}