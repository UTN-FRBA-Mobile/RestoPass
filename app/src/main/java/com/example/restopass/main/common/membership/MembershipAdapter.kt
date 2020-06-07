package com.example.restopass.main.common.membership

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Membership
import kotlinx.android.synthetic.main.view_membership_item.view.*

class MembershipAdapter(private val parentFragment: Fragment) :
    RecyclerView.Adapter<MembershipAdapter.MembershipViewHolder>() {

    var memberships: List<Membership> = listOf()

    class MembershipViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount() = memberships.size

    override fun getItemViewType(position: Int): Int {
        return if (memberships[position].isTitle)
            VIEWTYPE_TITLE
        else
            VIEWTYPE_MEMBERSHIP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipViewHolder {
        val layout =
            if (viewType == VIEWTYPE_TITLE) R.layout.view_membership_title else R.layout.view_membership_item

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return MembershipViewHolder(
            view
        )

    }

    override fun onBindViewHolder(holder: MembershipViewHolder, position: Int) {
        val membership = memberships[position]
        holder.itemView.apply {
            membershipTitle.text = membership.name

            if (!membership.isTitle) {
                priceTag.text = resources.getString(R.string.price_tag, membership.price.toString())

                Glide.with(this).load(membership.img).into(image)

                if (parentFragment is MembershipFragment) {
                    membershipCard.layoutParams.apply {
                        this.width = ViewGroup.LayoutParams.MATCH_PARENT
                        membershipCard.layoutParams = this
                    }

                    image.layoutParams.apply {
                        this.height =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 194.toFloat(), resources.displayMetrics).toInt()
                        image.layoutParams = this
                    }
                }

                description.text = membership.description

                restaurantsAmount.text = membership.restaurants?.size.toString()

                val dishes = membership.restaurants?.flatMap { it.dishes }?.size
                dishesAmount.text = dishes.toString()

                visitsAmount.text = membership.visits.toString()

                if (membership.isActual) {
                    membershipButton.setText(R.string.actual_membership)
                    membershipButton.isEnabled = false
                }
                detailsButton.setOnClickListener {
                    val bundle = bundleOf("membershipId" to membership.membershipId)
                    findNavController().navigate(R.id.restaurantsFragment, bundle)
                }
            }

        }
    }

    companion object {
        private const val VIEWTYPE_TITLE = 1
        private const val VIEWTYPE_MEMBERSHIP = 2
    }
}