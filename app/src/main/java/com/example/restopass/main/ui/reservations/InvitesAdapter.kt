package com.example.restopass.main.ui.reservations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import kotlinx.android.synthetic.main.invites_row.view.*

class InvitesAdapter(private val invitesFragment: ReservationCreateStepFourFragment) :
    RecyclerView.Adapter<InvitesHolder>() {

    var list: MutableList<Pair<String, String>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitesHolder {
        val inflater = LayoutInflater.from(parent.context)
        return InvitesHolder(inflater, parent, invitesFragment)
    }

    override fun onBindViewHolder(holder: InvitesHolder, position: Int) {
        val timesRow: Pair<String, String> = list[position]
        holder.bind(timesRow, this)
    }

    override fun getItemCount(): Int = list.size

}

class InvitesHolder(
    val inflater: LayoutInflater,
    parentCreateReservation: ViewGroup,
    val listener: InvitesInteractionListener?
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.invites_row, parentCreateReservation, false)) {

    fun bind(
        person: Pair<String, String>,
        invitesAdapter: InvitesAdapter
    ) {
        itemView.apply {
            this.inviteNameAndLastName.text = person.second + " (" + person.first + ")"
            this.deleteInviteIcon.setOnClickListener{
                invitesAdapter.list.remove(person)
                invitesAdapter.notifyDataSetChanged()
            }
        }
    }

    interface InvitesInteractionListener {
        fun nextAndSave(guests: Int)
    }


}
