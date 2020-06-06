package com.example.restopass.domain

import androidx.lifecycle.ViewModel
import com.example.restopass.main.ui.reservations.ReservationsFragment
import com.example.restopass.service.ReservationService
import java.time.LocalDateTime
import java.util.*

data class ReservationResponse(
    var reservations : List<Reservation> = listOf()
) : ViewModel() {

    suspend fun get() {
        ReservationService.getReservations().let {
            this.reservations = it;
        }
    }

    suspend fun cancel(reservationId : String) {
        ReservationService.cancelReservation(reservationId)
        get()
    }
}

data class Reservation(
    val reservationId : String,
    val restaurantId: String,
    val restaurantAddress : String,
    val restaurantName: String,
    val date : String,
    val state: String,
    val ownerUser : String,
    val toConfirmUsers: List<String>?,
    val confirmedUsers : List<String>?,
    val qrBase64: String?)

