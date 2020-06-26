package com.example.restopass.domain

import androidx.lifecycle.ViewModel
import com.example.restopass.service.MembershipService

data class MembershipsResponse(
    val actualMembership: MembershipResponse?,
    val memberships: MutableList<MembershipResponse>
)

data class MembershipResponse(
    val membershipInfo: MembershipInfo,
    var restaurants: List<Restaurant>
)

data class MembershipInfo(
    val membershipId: Int,
    val name: String,
    val description: String,
    val img: String,
    val visits: Number,
    val price: Number
)

class Membership(
    val membershipId: Int? = null,
    val name: String,
    val description: String? = null,
    val img: String? = null,
    val visits: Number? = null,
    val price: Number? = null,
    var restaurants: List<Restaurant>? = listOf(),
    var isActual: Boolean = false,
    val isTitle: Boolean = false) {

    fun dishesAmount() = restaurants?.flatMap { it.dishes }?.size
}

data class Memberships(
    var actualMembership: Membership?,
    var memberships: List<Membership>
)

class MembershipsViewModel : ViewModel() {
    var actualMembership: Membership? = null
    lateinit var memberships: MutableList<Membership>
    var wasEnrolled = false

    suspend fun get() {
        MembershipService.getMemberships().let {
            this.actualMembership = it.actualMembership
            this.memberships = it.memberships.toMutableList()
        }
    }

    suspend fun update(membership: Membership) {
        MembershipService.updateMembership(membership.membershipId!!).let {
           this.memberships = this.memberships.apply {
                actualMembership?.let { add(it) }
                remove(membership)
            }.sortedByDescending { it.membershipId }.toMutableList()

            this.actualMembership = membership

        }
    }
}

class SelectedMembershipViewModel : ViewModel() {
    var membership: Membership? = null
}

