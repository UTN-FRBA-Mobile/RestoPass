package com.example.restopass.main.ui.reservations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.domain.Reservation

class ReservationsAdapter(private val reservationsFragment: ReservationsFragment) : RecyclerView.Adapter<ReservationHolder>() {

    var list: List<Reservation> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ReservationHolder(inflater, parent, reservationsFragment)
    }

    override fun onBindViewHolder(holder: ReservationHolder, position: Int) {
        val reservation: Reservation = list[position]
        holder.bind(reservation)
    }

    override fun getItemCount(): Int = list.size

}