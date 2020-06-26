package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.CreateReservationViewModel
import com.example.restopass.domain.RestaurantConfigViewModel
import com.example.restopass.domain.RestaurantViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.reservation_create_step2.view.*
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationCalendarText
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationPickTime
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationRestaurantName
import kotlinx.android.synthetic.main.reservation_create_step2.view.restaurantImageReservation
import kotlinx.android.synthetic.main.reservation_create_step3.view.*

class ReservationCreateStepThreeFragment() : Fragment(), GuestsHolder.NextListener
{

    private lateinit var restaurantConfigViewModel: RestaurantConfigViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var createReservationViewModel: CreateReservationViewModel
    private lateinit var guestsAdapter: GuestsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reservation_create_step3, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        guestsAdapter = GuestsAdapter(this)

        val linearLayoutManager = LinearLayoutManager(activity)
        view.createReservationPickTime.apply {
            layoutManager = linearLayoutManager
            adapter = guestsAdapter
        }

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        restaurantConfigViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantConfigViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        Glide.with(this).load(restaurantViewModel.restaurant.img)
            .into(view.restaurantImageReservation)
        view.createReservationRestaurantName.text = restaurantViewModel.restaurant.name
        view.createReservationCalendarText.text = buildDate(createReservationViewModel.date)
        view.createReservationClockText.text = createReservationViewModel.hour + "hs"

        guestsAdapter.list = IntRange(1, restaurantConfigViewModel.restaurantConfig.maxDiners.toInt()).chunked(4)




    }

    private fun buildDate(date: CalendarDay): String {
        return date.day.toString() + "/" + date.month + "/" + date.year
    }

    override fun nextAndSave(guests: Int) {
        createReservationViewModel.guests = guests as Integer
        this.findNavController().navigate(R.id.reservationCreateStep4)
    }

}