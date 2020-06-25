package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.*
import com.prolificinteractive.materialcalendarview.*
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

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        restaurantConfigViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantConfigViewModel::class.java)

        createReservationViewModel =
            ViewModelProvider(requireActivity()).get(CreateReservationViewModel::class.java)

        Glide.with(this).load(restaurantViewModel.restaurant.img).into(view.restaurantImageReservation)
        view.createReservationRestaurantName.text = restaurantViewModel.restaurant.name

        coroutineScope.launch {
            try {
                Timber.i(restaurantViewModel.restaurant.restaurantId)
                restaurantConfigViewModel.get(restaurantViewModel.restaurant.restaurantId)
                view.calendarView.addDecorator(DisableFullDays(restaurantConfigViewModel.restaurantConfig.slots))
                view.calendarView.visibility = View.VISIBLE
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                }
            }
        }

        view.calendarView.setOnDateChangedListener(this)
    }


    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if(selected) {
            createReservationViewModel.date = date
            view?.findNavController()?.navigate(R.id.reservationCreateStep2)
        }
    }
}



class DisableFullDays(private val slots : List<RestaurantSlot>) : DayViewDecorator {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun shouldDecorate(day: CalendarDay?): Boolean {

        val slot : RestaurantSlot? = slots.find {
            val date : LocalDateTime = LocalDateTime.parse(it.dateTime[0][0].dateTime)
            date.dayOfMonth == day?.day && date.monthValue == day.month
        }

        return slot?.dayFull ?: true

    }

    override fun decorate(view: DayViewFacade?) {
        view?.setDaysDisabled(true)
    }

}


