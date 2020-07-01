package com.example.restopass.main.ui.reservations

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
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
import com.example.restopass.common.AppPreferences
import com.example.restopass.connection.ApiException
import com.example.restopass.domain.*
import com.example.restopass.login.domain.User
import com.example.restopass.main.common.AlertDialog
import com.example.restopass.service.UserService
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.invitation_error.view.*
import kotlinx.android.synthetic.main.reservation_create_step1.view.*
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
    private lateinit var invitesAdapter: InvitesAdapter

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

        invitesAdapter = InvitesAdapter(this)

        val linearLayoutManager = LinearLayoutManager(activity)
        view.createReservationInviteList.apply {
            layoutManager = linearLayoutManager
            adapter = invitesAdapter
        }

        if (createReservationViewModel.guestsList.isNotEmpty()) invitesAdapter.list =
            createReservationViewModel.guestsList.toMutableList()

        buildLocalDateTime(createReservationViewModel.date, createReservationViewModel.hour)

        view.apply {

            Glide.with(this).load(restaurantViewModel.restaurant.img)
                .into(restaurantImageReservation)

            val stringWithFormat = "Reserva para <b>" + createReservationViewModel.guests +
                    " personas </b> <br> en <b>" + restaurantViewModel.restaurant.name + "</b> <br> " +
                    "el <b>" + dateToHuman() + "</b> a las <b>" + createReservationViewModel.hour + "hs</b>"

            createReservationSummary.text = Html.fromHtml(stringWithFormat)
            createReservationSummary.visibility = View.VISIBLE

            createReservationInviteInputText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    view.createReservationInviteButton.isEnabled =
                        s.toString().trim { it <= ' ' }.length != 0
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                    // TODO Auto-generated method stub
                }

                override fun afterTextChanged(s: Editable) {
                    // TODO Auto-generated method stub
                }
            })

            createReservationSummary2.text = view.context.getString(
                R.string.confirm_reservation_disclaimer,
                getCancelHour(), getCancelDate()
            )


            createReservationInviteButton?.setOnClickListener {

                coroutineScope.launch {

                    if (invitesAdapter.list.any { it.second == (view.createReservationInviteInputText!!.text.toString()) }) {
                        val titleView: View =
                            layoutInflater.inflate(R.layout.invitation_error, container, false)
                        titleView.invitationErrorTitle.text = getString(R.string.already_add_invite)
                        AlertDialog.getAlertDialog(
                            titleView.context,
                            titleView
                        ).show()
                    } else if (invitesAdapter.list.size >= createReservationViewModel.guests.toInt() - 1) {
                        val titleView: View =
                            layoutInflater.inflate(R.layout.invitation_error, container, false)
                        titleView.invitationErrorTitle.text = getString(R.string.table_full)
                        AlertDialog.getAlertDialog(
                            titleView.context,
                            titleView
                        ).show()
                    } else {
                        try {
                            createReservationInviteButton.visibility = View.GONE
                            inviteLoader.visibility = View.VISIBLE
                            val user: User = UserService.checkCanAddToReservation(
                                createReservationInviteInputText!!.text.toString(),
                                getRestaurantBaseMembership(restaurantViewModel.restaurant)!!
                            )

                            invitesAdapter.list.add(
                                Pair(
                                    user.name + " " + user.lastName,
                                    user.email
                                )
                            )
                            invitesAdapter.notifyDataSetChanged()

                            inviteLoader.visibility = View.GONE
                            createReservationInviteButton.visibility = View.VISIBLE
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

            createReservationConfirmButton.setOnClickListener {
                createReservationViewModel.guestsList = invitesAdapter.list

                coroutineScope.launch {
                    try {
                        createReservationConfirmButton.visibility = View.GONE
                        confirmReservationLoader.visibility = View.VISIBLE
                        createReservationViewModel.send(restaurantViewModel.restaurant.restaurantId)
                        AppPreferences.user.visits = AppPreferences.user.visits - 1
                        findNavController().navigate(R.id.reservationCreateStep5)
                    } catch (e: Exception) {
                        if (isActive) {
                            Timber.e(e)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }

    override fun onStart() {
        super.onStart()
        job = Job()
        coroutineScope = CoroutineScope(job + Dispatchers.Main)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildLocalDateTime(date: CalendarDay, hour: String) {
        if (date.month < 10) {
            if(hour.split(":")[0].toInt() < 10) {
                createReservationViewModel.dateTime =
                    LocalDateTime.parse(date.year.toString() + "-0" + date.month.toString() + "-" + date.day.toString() + "T0" + hour + ":00")
            } else {
                createReservationViewModel.dateTime =
                    LocalDateTime.parse(date.year.toString() + "-0" + date.month.toString() + "-" + date.day.toString() + "T" + hour + ":00")
            }
        } else {
            if(hour.split("")[0].toInt() < 10) {
                createReservationViewModel.dateTime =
                    LocalDateTime.parse(date.year.toString() + "-" + date.month.toString() + "-" + date.day.toString() + "T0" + hour + ":00")
            } else {
                createReservationViewModel.dateTime =
                    LocalDateTime.parse(date.year.toString() + "-" + date.month.toString() + "-" + date.day.toString() + "T" + hour + ":00")
            }
        }
    }


    companion object {
        private lateinit var createReservationViewModel: CreateReservationViewModel

        @RequiresApi(Build.VERSION_CODES.O)
        fun dateToHuman(dt: LocalDateTime = createReservationViewModel.dateTime): String? {

            val dayName = dt.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                Locale("es")
            )

            val monthName = dt.month.getDisplayName(TextStyle.FULL, Locale("es"))

            return dayName.capitalize() + " " + dt.dayOfMonth + " de " + monthName.capitalize() + " de " + dt.year
        }
    }

    override fun nextAndSave(guests: Int) {
        TODO("Not yet implemented")
    }

    private fun getRestaurantBaseMembership(restaurant: Restaurant): Int? {
        return restaurant.dishes.map { it.baseMembership }.min()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCancelHour(): String {
        val dtCancel =
            createReservationViewModel.dateTime.minusHours(restaurantViewModel.restaurant.hoursToCancel.toLong())
        return if (dtCancel.minute == 0) {
            dtCancel.hour.toString() + ":" + "00"
        } else {
            dtCancel.hour.toString() + ":" + dtCancel.minute.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCancelDate(): String? {
        val dtCancel =
            createReservationViewModel.dateTime.minusHours(restaurantViewModel.restaurant.hoursToCancel.toLong())
        return dateToHuman(dtCancel)
    }

}