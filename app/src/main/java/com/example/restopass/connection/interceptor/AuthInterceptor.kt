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

class AuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response? {

        synchronized(this) {
            val originalRequest = chain.request()
            val request = originalRequest.withHeader("X-Auth-Token", AppPreferences.accessToken!!)

            val response = chain.proceed(request)

            return when {
                response.isSuccessful -> response
                else -> {
                    if (response.code() == 401) {
                        val rawJson = response.body()!!.string()

                        val apiError: ApiError = rawJson.fromJson()

                        if (apiError.status == 40101) {
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
//                                    val intent = Intent(Application().applicationContext, LoginActivity::class.java)
////                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
////                                    Application().applicationContext.startActivity(intent)
////                                    null
                                    response
                                }
                            }

                        }
                    }
                    return response
                }
            }
         }
    }

}



private fun Request.withHeader(name: String, value: String): Request {
    return this.newBuilder()
        .header(name, value)
        .build()
}