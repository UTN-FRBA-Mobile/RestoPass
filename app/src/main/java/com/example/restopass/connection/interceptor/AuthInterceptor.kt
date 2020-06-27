package com.example.restopass.connection.interceptor

import com.example.restopass.common.AppPreferences
import com.example.restopass.common.fromJson
import com.example.restopass.connection.ApiError
import com.example.restopass.login.domain.LoginResponse
import com.example.restopass.service.LoginService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

class AuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response? {
        synchronized(this) {
            val originalRequest = chain.request()
            val request = originalRequest.withHeader("X-Auth-Token", AppPreferences.accessToken!!)

            val response = chain.proceed(request)

            return when {
                response.isSuccessful -> response
                else -> {
                    var rawJson: String
                    if (response.code() == 401) {
                        rawJson = response.body()!!.string()

                        val apiError: ApiError = rawJson.fromJson()

                        if (apiError.code == 40101) {
                            return resolveExpiredAccessToken(originalRequest, chain)
                        }
                    }
                    rawJson = response.body()!!.string()
                    val apiError: ApiError = rawJson.fromJson()
                    throw IOException(apiError.message)
                }
            }
         }
    }

}

private fun resolveExpiredAccessToken(originalRequest: Request, chain: Interceptor.Chain) : Response? {
    Timber.i("Access token has expired. Trying to get new refresh and access ttoken")
    val responseRefreshToken = runBlocking {
        LoginService.refreshToken(AppPreferences.accessToken!!, AppPreferences.refreshToken!!)
    }

    return when {
        responseRefreshToken.isSuccessful -> {
            Timber.i("Refresh token request success. Setting new refresh and access token")

            val loginRequest: LoginResponse = responseRefreshToken.body()!!
            AppPreferences.apply {
                accessToken = loginRequest.xAuthToken
                refreshToken = loginRequest.xRefreshToken
                user = loginRequest.user
            }

            Timber.i("Trying to make same old request with new access token")
            val newAuthenticationRequest = originalRequest.withHeader("X-Auth-Token", AppPreferences.accessToken!!)
            chain.proceed(newAuthenticationRequest)
        }
        else -> {
            AppPreferences.logout()
            throw IOException()
        }
    }
}


private fun Request.withHeader(name: String, value: String): Request {
    return this.newBuilder()
        .header(name, value)
        .build()
}