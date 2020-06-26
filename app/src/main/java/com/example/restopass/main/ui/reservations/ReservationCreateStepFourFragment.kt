package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.connection.ApiException
import com.example.restopass.domain.CreateReservationViewModel
import com.example.restopass.domain.Restaurant
import com.example.restopass.domain.RestaurantConfigViewModel
import com.example.restopass.domain.RestaurantViewModel
import com.example.restopass.main.common.AlertDialog
import com.example.restopass.service.UserService
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.invitation_error.view.*
import kotlinx.android.synthetic.main.reservation_create_step2.view.restaurantImageReservation
import kotlinx.android.synthetic.main.reservation_create_step4.view.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*
class ReservationCreateStepFourFragment() : Fragment(), InvitesHolder.InvitesInteractionListener {

    private lateinit var restaurantConfigViewModel: RestaurantConfigViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var createReservationViewModel: CreateReservationViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reservation_create_step4, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        restaurantConfigViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantConfigViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        Glide.with(this).load(restaurantViewModel.restaurant.img)
            .into(view.restaurantImageReservation)

        val stringWithFormat = "Reserva para <b>" + createReservationViewModel.guests +
                " personas </b> <br> en <b>" + restaurantViewModel.restaurant.name + "</b> <br> " +
                "el <b>" + dateToHuman(createReservationViewModel.date) + "</b> a las <b>" + createReservationViewModel.hour + "hs</b>"

        view.createReservationSummary.text = Html.fromHtml(stringWithFormat)

        view?.createReservationInviteButton?.setOnClickListener {
            coroutineScope.launch {
                try {
                    UserService.checkCanAddToReservation(
                        view?.createReservationInviteInputText!!.text.toString(),
                        getRestaurantBaseMembership(restaurantViewModel.restaurant)!!
                    )
                } catch (e: ApiException) {

                    val titleView: View =
                        layoutInflater.inflate(R.layout.invitation_error, container, false)
                    titleView.invitationErrorTitle.text =
                        e.localizedMessage
                    AlertDialog.getAlertDialog(
                        titleView.context,
                        titleView
                    ).show()


                } catch (e: Exception) {
                    if (isActive) Timber.e(e)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateToHuman(date: CalendarDay): String? {
        lateinit var dt: LocalDateTime
        if (date.month < 10) {
            dt =
                LocalDateTime.parse(date.year.toString() + "-0" + date.month.toString() + "-" + date.day.toString() + "T00:00:00")
        } else {
            dt =
                LocalDateTime.parse(date.year.toString() + "-" + date.month.toString() + "-" + date.day.toString() + "T00:00:00")
        }

        val dayName = dt.dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale("es")
        )

        val monthName = dt.month.getDisplayName(TextStyle.FULL, Locale("es"))

        return dayName.capitalize() + " " + dt.dayOfMonth + " de " + monthName.capitalize() + " de " + dt.year
    }

    override fun nextAndSave(guests: Int) {
        TODO("Not yet implemented")
    }

    private fun getRestaurantBaseMembership(restaurant: Restaurant): Int? {
        return restaurant.dishes.map { it.baseMembership }.max()
    }

}