package com.example.restopass.main.ui.reservations

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.domain.CreateReservationViewModel
import com.example.restopass.domain.RestaurantViewModel
import kotlinx.android.synthetic.main.reservation_create_step1.view.*
import kotlinx.android.synthetic.main.reservation_create_step2.view.restaurantImageReservation
import kotlinx.android.synthetic.main.reservation_create_step5.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class ReservationCreateStepFiveFragment() : Fragment() {

    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var createReservationViewModel: CreateReservationViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reservation_create_step5, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        view.apply {
            Glide.with(this).load(restaurantViewModel.restaurant.img)
                .into(restaurantImageReservation)

            reservationSuccessRestaurant.text = restaurantViewModel.restaurant.name + "\n" + restaurantViewModel.restaurant.address
            reservationSuccessDayText.text = ReservationCreateStepFourFragment.dateToHuman(createReservationViewModel.dateTime)
            reservationSuccessTimeText.text = context.getString(
                R.string.hours,
                createReservationViewModel.hour
            )
            createReservationGuestsText.text = context.getString(
                R.string.guests_list,
                createReservationViewModel.guests.toString()
            )

            reservationSuccessTitle.text = context.getString(
                R.string.confirm_reservation_title,
                AppPreferences.user.name
            )

            if(createReservationViewModel.guestsList.isNotEmpty()) {
                createReservationGuestsText.text = createReservationGuestsText.text.toString() + " con\n" +
                        createReservationViewModel.guestsList.joinToString("\n") { it.first }
            }

            reservationSuccessShare.setOnClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBody = buildShareBody()
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, "Compartir vía"))
            }

            goToHomeButton.setOnClickListener{
                if(AppPreferences.user.actualMembership != null) {
                    findNavController().navigate(R.id.navigation_enrolled_home)
                } else {
                    findNavController().navigate(R.id.navigation_not_enrolled_home)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildShareBody() : String? {
        return view?.context?.getString(R.string.share_body_reservation,
            createReservationViewModel.guests.toString(), ReservationCreateStepFourFragment.dateToHuman(createReservationViewModel.dateTime),
        createReservationViewModel.hour, restaurantViewModel.restaurant.name, restaurantViewModel.restaurant.address)
    }
}