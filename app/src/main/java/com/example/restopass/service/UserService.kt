package com.example.restopass.service

import com.example.restopass.common.error
import com.example.restopass.common.fromJson
import com.example.restopass.connection.ApiError
import com.example.restopass.connection.ApiException
import com.example.restopass.connection.RetrofitFactory
import com.example.restopass.login.domain.User
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path
import timber.log.Timber
import java.io.IOException

object UserService {
    private const val BASE_URL = "https://restopass.herokuapp.com"

    interface UserApi {
        @POST("/users/favorite/{restaurantId}")
        fun favRestaurant(@Path("restaurantId") restaurantId: String): Deferred<Response<Void>>

        @POST("/users/unfavorite/{restaurantId}")
        fun unFavRestaurant(@Path("restaurantId") restaurantId: String): Deferred<Response<Void>>

        @POST("/users/check/{userId}/{baseMembership}")
        fun checkCanAddToReservation(
            @Path("userId") userId: String,
            @Path("baseMembership") baseMembership: Int
        ): Deferred<Response<User>>


    }

    private var api: UserApi

    init {
        api = RetrofitFactory.createClient(BASE_URL, UserApi::class.java)
    }

    suspend fun favorite(restaurantId: String) {
        val response = api.favRestaurant(restaurantId).await()
        Timber.i("Executed POST. Response code was ${response.code()}")

        if (!response.isSuccessful) throw response.error()
    }

    suspend fun unfavorite(restaurantId: String) {
        val response = api.unFavRestaurant(restaurantId).await()
        Timber.i("Executed POST. Response code was ${response.code()}")

        if (!response.isSuccessful) throw response.error()
    }

    suspend fun checkCanAddToReservation(userId: String, baseMembership: Int): User {
        try {
            val response = api.checkCanAddToReservation(userId, baseMembership).await()
            Timber.i("Executed POST. Response code was ${response.code()}")

            return when {
                response.isSuccessful -> response.body()!!
                else -> throw response.error()
            }
        } catch (e: IOException) {
            if (e.localizedMessage !== null) throw ApiException(ApiError(1,1,e.localizedMessage!!))
            else throw e
        }
    }
}