package com.example.restopass.domain

import androidx.lifecycle.ViewModel
import com.example.restopass.main.ui.reservations.ReservationsFragment
import com.example.restopass.service.ReservationService
import java.time.LocalDateTime
import java.util.*

class ReservationViewModel : ViewModel() {

    lateinit var reservations : List<Reservation>

    suspend fun get() {
        ReservationService.getReservations().let {
            this.reservations = it;
        }
    }

    suspend fun cancel(reservationId : String) {
        ReservationService.cancelReservation(reservationId).let {
            this.reservations = it
        }
    }
}

data class Reservation(
    val reservationId : String,
    val restaurantId: String,
    val restaurantAddress : String,
    val restaurantName: String,
    val date : String,
    val state: String,
    val ownerUser : UserReservation,
    val toConfirmUsers: List<UserReservation>?,
    val confirmedUsers : List<UserReservation>?,
    val qrBase64: String?,
    val isInvitation : Boolean)

data class UserReservation(
    val userId : String,
    val name : String,
    val lastName : String
)
