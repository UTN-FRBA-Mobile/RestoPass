package com.example.restopass.main.ui.reservations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.google.android.material.button.MaterialButton

class TimesAdapter(private val timesFragment: ReservationCreateStepTwoFragment) :
    RecyclerView.Adapter<TimesHolder>() {

    var list: List<List<Pair<String, Boolean>>> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TimesHolder(inflater, parent, timesFragment)
    }

    override fun onBindViewHolder(holder: TimesHolder, position: Int) {
        val timesRow: List<Pair<String, Boolean>> = list[position]
        holder.bind(timesRow)
    }

    override fun getItemCount(): Int = list.size

}

class TimesHolder(
    val inflater: LayoutInflater,
    parentCreateReservation: ViewGroup,
    val listener: NextListener?
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.times_row, parentCreateReservation, false)) {

    fun bind(timesRow: List<Pair<String, Boolean>>) {
        itemView.apply {
            for (x in timesRow.indices) {
                var button = this.findViewWithTag("hourButton$x") as MaterialButton
                button.text = timesRow[x].first
                button.visibility = View.VISIBLE
                button.setOnClickListener {
                    listener?.nextAndSave(timesRow[x].first)
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
        fun nextAndSave(hour: String)
    }


}

