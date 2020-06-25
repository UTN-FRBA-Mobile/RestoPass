package com.example.restopass.main.ui.reservations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.google.android.material.button.MaterialButton

class GuestsAdapter(private val guestsFragment: ReservationCreateStepThreeFragment) :
    RecyclerView.Adapter<GuestsHolder>() {

    var list: List<List<Int>> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestsHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GuestsHolder(inflater, parent, guestsFragment)
    }

    override fun onBindViewHolder(holder: GuestsHolder, position: Int) {
        val timesRow: List<Int> = list[position]
        holder.bind(timesRow)
    }

    override fun getItemCount(): Int = list.size

}

class GuestsHolder(
    val inflater: LayoutInflater,
    parentCreateReservation: ViewGroup,
    val listener: NextListener?
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.times_row, parentCreateReservation, false)) {

    fun bind(timesRow: List<Int>) {
        itemView.apply {
            for (x in timesRow.indices) {
                var button = this.findViewWithTag("guestButton$x") as MaterialButton
                button.text = timesRow[x].toString()
                button.visibility = View.VISIBLE
                button.setOnClickListener {
                    listener?.nextAndSave(timesRow[x])
                }
                if (timesRow[x].second) {
                    button.isEnabled = false
                    button.setBackgroundColor(resources.getColor(R.color.disableButtonColor))
                    button.setTextColor(resources.getColor(R.color.borderGray))
                }
            }
        }
    }

    interface NextListener {
        fun nextAndSave(guests: String)
    }


}
