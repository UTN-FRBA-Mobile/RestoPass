package com.example.restopass.service

import com.example.restopass.common.error
import com.example.restopass.connection.RetrofitFactory
import com.example.restopass.domain.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import timber.log.Timber


object MembershipService {
    private var api: RestopassApi = RetrofitFactory.createClient(BASE_URL, RestopassApi::class.java)
    
    interface RestopassApi {
        @GET("/memberships")
        fun getMembershipsAsync(): Deferred<Response<MembershipsResponse>>

        @PATCH("/memberships/users")
        fun updateMembership(@Body membershipId: UpdateMembershipRequest): Deferred<Response<Void>>
    }

    suspend fun getMemberships(): Memberships {
        val response = api.getMembershipsAsync().await()
        Timber.i("Executed POST to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!.toClient()
            else -> throw response.error()
        }
    }

    suspend fun updateMembership(membershipId: Int) {
        val response = api.updateMembership(UpdateMembershipRequest(membershipId)).await()
        Timber.i("Executed POST. Response code was ${response.code()}")

        if (!response.isSuccessful) throw response.error()
    }

    private fun MembershipsResponse.toClient(): Memberships {

        val actualMembership = this.actualMembership?.toClient()
        val memberships = this.memberships.map {
            it.toClient()
        }.sortedByDescending { it.membershipId }
        return Memberships(
            actualMembership,
            memberships
        )
    }

    private fun MembershipResponse.toClient(): Membership {
        val restaurantsWithAccordingDishes =
            this.restaurants.map { it.dishesByMembership(this.membershipInfo.membershipId) }

        return membershipInfo.let {
            Membership(
                it.membershipId,
                it.name,
                it.description,
                it.img,
                it.visits,
                it.price.toInt(),
                restaurantsWithAccordingDishes
            )
        }
    }

    private fun Restaurant.dishesByMembership(membershipId: Int): Restaurant {
        return this.copy(dishes = this.dishes.filter {
            it.isIncluded(membershipId)
        }
        )
    }

    data class UpdateMembershipRequest(val membershipId: Int)
}