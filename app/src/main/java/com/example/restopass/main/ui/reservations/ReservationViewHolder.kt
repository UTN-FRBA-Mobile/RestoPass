package com.example.restopass.main.ui.reservations

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Reservation
import com.example.restopass.main.common.AlertDialog
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class ReservationHolder(
    val inflater: LayoutInflater,
    val parent: ViewGroup,
    private val reservationsFragment: ReservationsFragment
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.reservations_list_items, parent, false)) {

    private var mCardView : CardView? = null;
    private var mImageView: ImageView? = null
    private var mTitleView: TextView? = null
    private var mAddressView: TextView? = null
    private var mDateView: TextView? = null
    private var mStatusView: TextView? = null
    private var mActionView: TextView? = null
    private var mQrView: TextView? = null
    private var mQrImageView: ImageView? = null
    private var mDinersView: TextView? = null
    private var mArrowView: ImageView? = null

    init {
        mCardView = itemView.findViewById(R.id.reservation_card)
        mImageView = itemView.findViewById(R.id.reservation_image)
        mTitleView = itemView.findViewById(R.id.reservation_title)
        mAddressView = itemView.findViewById(R.id.reservation_address)
        mDateView = itemView.findViewById(R.id.reservation_date)
        mStatusView = itemView.findViewById(R.id.reservation_status)
        mActionView = itemView.findViewById(R.id.reservation_action)
        mQrView = itemView.findViewById(R.id.qr_button)
        mDinersView = itemView.findViewById(R.id.reservation_diners)
        mArrowView = itemView.findViewById(R.id.reservation_arrow)
    }

    fun bind(reservation: Reservation) {
        Glide.with(itemView.context).load(R.drawable.pastas).into(mImageView!!);
        mTitleView?.text = reservation.restaurantName
        mAddressView?.text = reservation.restaurantAddress
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDateView?.text = dateToHuman(reservation.date, ((reservation.toConfirmUsers?.size ?: 0) + (reservation.confirmedUsers?.size ?: 0) +1))
        } else {
            mDateView?.text = reservation.date
        }
        if(reservation.state == "DONE") {
            mActionView?.setText(R.string.reservation_action_review)
            mStatusView?.setText(R.string.reservation_status_done)
            mCardView?.setBackgroundColor(Color.GRAY)
        }

        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        reservationsFragment.cancelReservation(reservation.reservationId)
                    }
                }
            }

        if(reservation.state == "CONFIRMED") {
            mActionView?.setText(R.string.reservation_action_cancel)
            mActionView?.setOnClickListener {
                val builder = androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                builder.setMessage(R.string.reservation_cancel_alert).setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }

            mQrView?.setText(R.string.reservation_show_qr)
            mQrView?.setOnClickListener {

                val titleView: View =
                    inflater.inflate(R.layout.qr_dialog, parent, false)

                mQrImageView = titleView.findViewById(R.id.qr_image)
                Glide.with(titleView.context).load(decodeQr(reservation.qrBase64)).fitCenter().into(mQrImageView!!)

                AlertDialog.getAlertDialog(
                    itemView.context,
                    titleView
                ).show()
            }
            mStatusView?.setText(R.string.reservation_status_confirmed)
            mStatusView?.setTextColor(Color.parseColor("#00b686"))
            mCardView?.setBackgroundColor(Color.parseColor("#00b686"))

        }

        if(reservation.state == "CANCELED") {
            mActionView?.setText(R.string.reservation_action_canceled)
            mStatusView?.setText(R.string.reservation_status_canceled)
            mStatusView?.setTextColor(Color.parseColor("#d11a2a"))
            mCardView?.setBackgroundColor(Color.parseColor("#d11a2a"))
            mQrView?.visibility = View.GONE
        }

        mArrowView?.setOnClickListener {

            if(mDinersView?.visibility!! == View.GONE) {
                mDinersView?.setText(R.string.reservation_cancel_alert)
                mDinersView?.visibility = View.VISIBLE
                Glide.with(itemView.context).load(R.drawable.ic_arrow_up_24dp).fitCenter().into(mArrowView!!)
            } else {
                mDinersView?.visibility = View.GONE
                Glide.with(itemView.context).load(R.drawable.ic_arrow_down_24dp).fitCenter().into(mArrowView!!)
            }
        }
    }

    private fun decodeQr(qrBase64 : String?) : ByteArray? {
        val pureBase64Encoded = qrBase64?.substring(qrBase64.indexOf(",")  + 1)
        return Base64.decode(pureBase64Encoded, Base64.DEFAULT)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateToHuman(date: String, diners : Int) : String? {
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


}