package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.TransitionManager
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.CreateReservationViewModel
import com.example.restopass.domain.RestaurantConfigViewModel
import com.example.restopass.domain.RestaurantSlot
import com.example.restopass.domain.RestaurantViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.reservation_create_step1.view.*
import kotlinx.android.synthetic.main.reservation_create_step2.view.*
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationClock
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationRestaurantName
import kotlinx.android.synthetic.main.reservation_create_step2.view.createReservationSteps
import kotlinx.android.synthetic.main.reservation_create_step2.view.restaurantImageReservation
import java.time.LocalDateTime


class ReservationCreateStepTwoFragment() : Fragment(), TimesHolder.NextListener {

    private lateinit var restaurantConfigViewModel: RestaurantConfigViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var createReservationViewModel: CreateReservationViewModel
    private lateinit var timesAdapter: TimesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reservation_create_step2, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timesAdapter = TimesAdapter(this)

        val linearLayoutManager = LinearLayoutManager(activity)
        view.createReservationPickTime.apply {
            layoutManager = linearLayoutManager
            adapter = timesAdapter
        }

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        restaurantConfigViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantConfigViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        view.apply {


            val slide = Slide()
            slide.slideEdge = Gravity.TOP

            TransitionManager.beginDelayedTransition(createReservationSteps,slide);
            createReservationCalendarText.visibility = View.VISIBLE;

            Glide.with(this).load(restaurantViewModel.restaurant.img)
                .into(restaurantImageReservation)
            createReservationRestaurantName.text = restaurantViewModel.restaurant.name
            createReservationCalendarText.text = buildDate(createReservationViewModel.date)
            createReservationClock.setImageResource(R.drawable.greenclock)

            var slot: RestaurantSlot? = restaurantConfigViewModel.restaurantConfig.slots.find {
                val date: LocalDateTime = LocalDateTime.parse(it.dateTime[0][0].dateTime)
                date.dayOfMonth == createReservationViewModel.date.day && date.monthValue == createReservationViewModel.date.month
            }

            val times = slot?.dateTime?.flatMap {
                it.map {
                    val localDateTime = LocalDateTime.parse(it.dateTime)
                    val emptyTime = it.tablesAvailable == 0
                    if (localDateTime.minute == 0) {
                        Pair(localDateTime.hour.toString() + ":00", emptyTime)
                    } else {
                        Pair(
                            localDateTime.hour.toString() + ":" + localDateTime.minute.toString(),
                            emptyTime
                        )
                    }
                }
            }

            timesAdapter.list = times?.chunked(3)!!

        }

    }

    private fun buildDate(date: CalendarDay): String {
        return date.day.toString() + "/" + date.month + "/" + date.year
    }

    override fun nextAndSave(hour: String) {
        createReservationViewModel.hour = hour
        this.findNavController().navigate(R.id.reservationCreateStep3)
    }


}
