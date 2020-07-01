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
import androidx.navigation.findNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.CreateReservationViewModel
import com.example.restopass.domain.RestaurantConfigViewModel
import com.example.restopass.domain.RestaurantSlot
import com.example.restopass.domain.RestaurantViewModel
import com.prolificinteractive.materialcalendarview.*
import kotlinx.android.synthetic.main.fragment_membership.*
import kotlinx.android.synthetic.main.reservation_create_step1.view.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDateTime

class ReservationCreateStepOneFragment() : Fragment(), OnDateSelectedListener {

    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var restaurantConfigViewModel: RestaurantConfigViewModel
    private lateinit var createReservationViewModel: CreateReservationViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reservation_create_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onStop() {
        super.onStop()
        job.cancel()
    }

    override fun onStart() {
        super.onStart()
        job = Job()
        coroutineScope = CoroutineScope(job + Dispatchers.Main)

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        restaurantConfigViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantConfigViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        createReservationViewModel.guestsList = listOf()

        view?.apply {

            Glide.with(this).load(restaurantViewModel.restaurant.img)
                .into(restaurantImageReservation)
            createReservationRestaurantName.text = restaurantViewModel.restaurant.name

            coroutineScope.launch {
                try {
                    calendarLoader.visibility = View.VISIBLE
                    Timber.i(restaurantViewModel.restaurant.restaurantId)
                    restaurantConfigViewModel.get(restaurantViewModel.restaurant.restaurantId)
                    calendarView.addDecorator(DisableFullDays(restaurantConfigViewModel.restaurantConfig.slots))
                    calendarView.visibility = View.VISIBLE
                    calendarLoader.visibility=View.GONE
                } catch (e: Exception) {
                    if (isActive) {
                        Timber.e(e)
                    }
                }
            }

            calendarView.setOnDateChangedListener(this@ReservationCreateStepOneFragment)

        }

    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if (selected) {
            createReservationViewModel.date = date
            view?.findNavController()?.navigate(R.id.reservationCreateStep2)
        }
    }
}


class DisableFullDays(private val slots: List<RestaurantSlot>) : DayViewDecorator {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun shouldDecorate(day: CalendarDay?): Boolean {

        val slot: RestaurantSlot? = slots.find {
            val date: LocalDateTime = LocalDateTime.parse(it.dateTime[0][0].dateTime)
            date.dayOfMonth == day?.day && date.monthValue == day.month
        }

        return slot?.dayFull ?: true

    }

    override fun decorate(view: DayViewFacade?) {
        view?.setDaysDisabled(true)
    }

}


