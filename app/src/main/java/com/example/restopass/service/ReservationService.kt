package com.example.restopass.service

import com.example.restopass.common.error
import com.example.restopass.connection.RetrofitFactory
import com.example.restopass.domain.Reservation
import com.example.restopass.domain.ReservationResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import timber.log.Timber

object ReservationService {
    private const val BASE_URL = "https://restopass.herokuapp.com/"

    interface ReservationApi{
        @GET("/reservations")
        fun getReservations():
                Deferred<Response<List<Reservation>>>
    }

    private var api: ReservationApi

    init {
        api = RetrofitFactory.createClient(BASE_URL, ReservationApi::class.java)
    }

    suspend fun getReservations(): List<Reservation> {
        val response = api.getReservations().await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }
}