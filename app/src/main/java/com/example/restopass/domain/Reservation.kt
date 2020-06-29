package com.example.restopass.domain

import androidx.lifecycle.ViewModel
import com.example.restopass.main.ui.reservations.ReservationCreateStepFourFragment
import com.example.restopass.service.ReservationService
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDateTime

class ReservationViewModel : ViewModel() {

    lateinit var reservations: List<Reservation>

    suspend fun get() {
        ReservationService.getReservations().let {
            this.reservations = it;
        }
    }

    suspend fun cancel(reservationId: String) {
        ReservationService.cancelReservation(reservationId).let {
            this.reservations = it
        }
    }
}

data class Reservation(
    val reservationId: String,
    val restaurantId: String,
    val restaurantAddress: String,
    val restaurantName: String,
    val img: String?,
    val date: String,
    val state: String,
    val ownerUser: UserReservation,
    val toConfirmUsers: List<UserReservation>?,
    val confirmedUsers: List<UserReservation>?,
    val qrBase64: String?,
    val isInvitation: Boolean
)

data class UserReservation(
    val userId: String,
    val name: String,
    val lastName: String
)

class CreateReservationViewModel : ViewModel() {
    lateinit var date: CalendarDay
    lateinit var hour: String
    lateinit var guests: Integer
    lateinit var dateTime: LocalDateTime
    var guestsList: List<Pair<String, String>> = listOf()

    suspend fun send(restaurantId: String) {
        val createReservationRequest = CreateReservationRequest(restaurantId, dateTime.toString(), guestsList.map { it.second })
        ReservationService.createReservation(createReservationRequest)
    }
}

class RestaurantConfigViewModel : ViewModel() {
    lateinit var restaurantConfig: RestaurantConfig;

    suspend fun get(restaurantId: String) {
        ReservationService.getRestaurantConfig(restaurantId).let {
            this.restaurantConfig = it
        }
    }
}

data class RestaurantConfig(
    val restaurantId: String,
    val tablesPerShift: Integer,
    val minutesGap: Integer,
    val slots: List<RestaurantSlot>,
    val maxDiners: Integer
)

data class RestaurantSlot(
    val dateTime: List<List<DateTimeWithTables>>,
    val dayFull: Boolean
)

data class DateTimeWithTables(
    val dateTime: String,
    val tablesAvailable: Int
)

data class CreateReservationRequest(
    val restaurantId: String,
    val date: String,
    val toConfirmUsers: List<String>
)
