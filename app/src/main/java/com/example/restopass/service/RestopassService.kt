package com.example.restopass.service

import com.example.restopass.common.error
import com.example.restopass.connection.RetrofitFactory
import com.example.restopass.domain.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber


object RestopassService {
    private const val BASE_URL = "https://restopass.herokuapp.com/"

    interface RestopassApi {
        @GET("/memberships")
        fun getMembershipsAsync():
                Deferred<Response<MembershipsResponse>>
        @GET("restaurants/{lat}/{lng}")
        fun getRestaurantForLocation(@Path("lat") latitude: Double, @Path("lng") longitude: Double):
                Deferred<Response<List<Restaurant>>>
    }

    private var api: RestopassApi

    init {
        api = RetrofitFactory.createClient(BASE_URL, RestopassApi::class.java)
    }

    suspend fun getMemberships(): Memberships {
        val response = api.getMembershipsAsync().await()
        Timber.i("Executed POST to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!.toClient()
            else -> throw response.error()
        }
    }

    private fun MembershipsResponse.toClient(): Memberships {
        val actualMembership = this.actualMembership?.toClient()
        val memberships = this.memberships.map {
            it.toClient()
        }.sortedByDescending { it.membershipId }
            .toMutableList()
        return Memberships(
            actualMembership,
            memberships
        )
    }

    private fun MembershipResponse.toClient(): Membership {
        val restaurantsWithAccordingDishes =
            this.restaurants!!.map { it.dishesByMembershipType(this.membershipInfo!!.membershipId)}

        return membershipInfo!!.let {
            Membership(
                it.membershipId,
                it.name,
                it.description,
                it.img,
                it.visits,
                it.price,
                restaurantsWithAccordingDishes
            )
        }
    }

    private fun Restaurant.dishesByMembershipType(membershipType: MembershipType): Restaurant {
        return this.copy(dishes = this.dishes.filter {
                    membershipType in it.topMembership.greaterMemberships().plus(it.topMembership)
            }
        )
    }

}