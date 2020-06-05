package com.example.restopass.main.ui.reservations

import android.graphics.Color
import android.media.Image
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Reservation
import com.example.restopass.main.common.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.qr_dialog.view.*

class ReservationHolder(
    val inflater: LayoutInflater,
    val parent : ViewGroup
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

    init {
        mCardView = itemView.findViewById(R.id.reservation_card)
        mImageView = itemView.findViewById(R.id.reservation_image)
        mTitleView = itemView.findViewById(R.id.reservation_title)
        mAddressView = itemView.findViewById(R.id.reservation_address)
        mDateView = itemView.findViewById(R.id.reservation_date)
        mStatusView = itemView.findViewById(R.id.reservation_status)
        mActionView = itemView.findViewById(R.id.reservation_action)
        mQrView = itemView.findViewById(R.id.qr_button)
    }

    fun bind(reservation: Reservation) {
        Glide.with(itemView.context).load(R.drawable.pastas).into(mImageView!!);
        mTitleView?.text = reservation.restaurantName
        mAddressView?.text = reservation.restaurantAddress
        mDateView?.text = reservation.date.toString()
        if(reservation.state == "DONE") {
            mActionView?.setText(R.string.reservation_action_review)
            mStatusView?.setText(R.string.reservation_status_done)
            mCardView?.setBackgroundColor(Color.GRAY)
        }

        if(reservation.state == "CONFIRMED") {
            mActionView?.setText(R.string.reservation_action_cancel)
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
        }
    }

    private fun decodeQr(qrBase64 : String?) : ByteArray? {
        val pureBase64Encoded = qrBase64?.substring(qrBase64.indexOf(",")  + 1)
        return Base64.decode(pureBase64Encoded, Base64.DEFAULT)
    }
}