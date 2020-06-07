package com.example.restopass.main.ui.reservations

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Reservation
import com.example.restopass.domain.UserReservation
import com.example.restopass.main.common.AlertDialog
import com.google.android.gms.common.util.Strings
import kotlinx.android.synthetic.main.qr_dialog.view.*
import kotlinx.android.synthetic.main.reservations_list_items.view.*
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class ReservationHolder(
    val inflater: LayoutInflater,
    val parentReservation: ViewGroup,
    private val reservationsFragment: ReservationsFragment
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.reservations_list_items, parentReservation, false)) {

    fun bind(reservation: Reservation) {

        itemView.apply {
            Glide.with(itemView.context).load(R.drawable.pastas).into(reservationImage!!);
            reservationTitle?.text = reservation.restaurantName
            reservationAddress?.text = reservation.restaurantAddress

            if (reservation.isInvitation) {
                val msg = itemView.context.getString(
                    R.string.reservation_invitation,
                    transformName(reservation.ownerUser)
                )
                itemView.reservationInvitation?.text = msg
                reservationInvitation?.visibility = View.VISIBLE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reservationDate?.text = dateToHuman(
                    reservation.date,
                    ((reservation.toConfirmUsers?.size ?: 0) + (reservation.confirmedUsers?.size
                        ?: 0) + 1)
                )
            } else {
                reservationDate?.text = reservation.date
            }
            if (reservation.state == "DONE") {
                reservationAction?.setText(R.string.reservation_action_review)
                reservationStatus?.setText(R.string.reservation_status_done)
                reservationCard?.setBackgroundColor(Color.GRAY)
            }

            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            reservationsFragment.cancelReservation(reservation.reservationId)
                        }
                    }
                }

            if (reservation.state == "CONFIRMED") {
                reservationAction?.setText(R.string.reservation_action_cancel)
                reservationAction?.setOnClickListener {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    builder.setMessage(R.string.reservation_cancel_alert)
                        .setPositiveButton("Si", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show()
                }

                reservationQrButton?.setText(R.string.reservation_show_qr)
                reservationQrButton?.setOnClickListener {

                    val titleView: View =
                        inflater.inflate(R.layout.qr_dialog, parentReservation, false)

                    Glide.with(titleView.context).load(decodeQr(reservation.qrBase64)).fitCenter()
                        .into(qrImage!!)

                    AlertDialog.getAlertDialog(
                        itemView.context,
                        titleView
                    ).show()
                }
                reservationStatus?.setText(R.string.reservation_status_confirmed)
                reservationStatus?.setTextColor(Color.parseColor("#00b686"))
                reservationCard?.setBackgroundColor(Color.parseColor("#00b686"))

            }

            if (reservation.state == "CANCELED") {
                reservationAction?.setText(R.string.reservation_action_canceled)
                reservationStatus?.setText(R.string.reservation_status_canceled)
                reservationStatus?.setTextColor(Color.parseColor("#d11a2a"))
                reservationCard?.setBackgroundColor(Color.parseColor("#d11a2a"))
                reservationQrButton?.visibility = View.GONE
            }

            reservationArrow?.setOnClickListener {
                if (reservationDinersModule?.visibility!! == View.GONE) {
                    reservationDinersModule?.visibility = View.VISIBLE
                    Glide.with(itemView.context).load(R.drawable.ic_arrow_up_24dp).fitCenter()
                        .into(reservationArrow!!)
                } else {
                    reservationDinersModule?.visibility = View.GONE
                    Glide.with(itemView.context).load(R.drawable.ic_arrow_down_24dp).fitCenter()
                        .into(reservationArrow!!)
                }
            }

            var confirmedUsersString = "";
            var toConfirmUsersString = "";
            reservation.toConfirmUsers?.forEach {
                toConfirmUsersString = toConfirmUsersString.plus("\n" + transformName(it))
            }
            reservation.confirmedUsers?.forEach {
                confirmedUsersString = confirmedUsersString.plus("\n" + transformName(it))
            }

            confirmedUsersString = confirmedUsersString.removePrefix("\n")
            toConfirmUsersString = toConfirmUsersString.removePrefix("\n")
            if (!Strings.isEmptyOrWhitespace(confirmedUsersString) || !Strings.isEmptyOrWhitespace(
                    toConfirmUsersString
                )
            ) {

                if (!Strings.isEmptyOrWhitespace(confirmedUsersString)) {
                    reservationDinersConfirmTitle?.setText(R.string.reservation_diners_confirmed)
                    reservationDinersConfirm?.text = confirmedUsersString
                } else {
                    reservationDinersConfirmTitle?.visibility = View.GONE
                    reservationDinersConfirm?.visibility = View.GONE
                }

                if (!Strings.isEmptyOrWhitespace(toConfirmUsersString)) {
                    reservationDinersToConfirmTile?.setText(R.string.reservation_diners_toConfirm)
                    reservationDinersToConfirm?.text = toConfirmUsersString
                } else {
                    reservationDinersToConfirmTile?.visibility = View.GONE
                    reservationDinersToConfirm?.visibility = View.GONE
                }

            }

        }
    }

    private fun decodeQr(qrBase64: String?): ByteArray? {
        val pureBase64Encoded = qrBase64?.substring(qrBase64.indexOf(",") + 1)
        return Base64.decode(pureBase64Encoded, Base64.DEFAULT)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateToHuman(date: String, diners: Int): String? {
        val dt = LocalDateTime.parse(date)

        val dayName = dt.dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale("es")
        )

        val monthName = dt.month.getDisplayName(TextStyle.FULL, Locale("es"))

        val hour: String
        hour = if (dt.minute == 0) {
            dt.hour.toString() + ":00"
        } else {
            dt.hour.toString() + ":" + dt.minute
        }

        return dayName.capitalize() + " " + dt.dayOfMonth + " de " + monthName.capitalize() + " de " + dt.year + " a las " + hour + "hs " + diners + "pers"
    }

    private fun transformName(user: UserReservation): String {
        return user.name + " " + user.lastName
    }


}