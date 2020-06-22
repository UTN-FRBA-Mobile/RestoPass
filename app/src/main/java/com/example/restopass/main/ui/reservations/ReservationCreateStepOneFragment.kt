package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.android.synthetic.main.reservation_create_step1.view.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDateTime

class ReservationCreateStepOneFragment() : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var restaurantViewModel: RestaurantViewModel
    private lateinit var restaurantConfigViewModel: RestaurantConfigViewModel

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

        Glide.with(this).load(restaurantViewModel.restaurant.img).into(view.restaurantImageReservation)
        view.createReservationCalendar.setImageResource(R.drawable.calendar)
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



    }



    interface OnFragmentInteractionListener {
        fun showFragment(fragment: Fragment)
    }


}

class DisableFullDays(private val slots : List<RestaurantSlot>) : DayViewDecorator {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun shouldDecorate(day: CalendarDay?): Boolean {

        var slot : RestaurantSlot? = slots.find {
            var date : LocalDateTime = LocalDateTime.parse(it.dateTime[0][0].dateTime)
            date.dayOfMonth == day?.day && date.monthValue == day.month
        }

        return slot?.dayFull ?: true

    }

    override fun decorate(view: DayViewFacade?) {
        view?.setDaysDisabled(true)
    }

    
}


