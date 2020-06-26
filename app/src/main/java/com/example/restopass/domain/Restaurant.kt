package com.example.restopass.domain

import androidx.lifecycle.ViewModel
import com.example.restopass.service.RestaurantService

data class Restaurant(
    val restaurantId: String,
    val name: String,
    val img: String,
    val address: String,
    val location: Location,
    val tags: List<String>,
    val timeTable: List<TimeTable>,
    val dishes: List<Dish>,
    val stars: Double
)

data class TimeTable(val openingDays: List<String>, val pairHours: List<PairHour>?)

class Dish(val dishId: String, val name: String, val description: String, val baseMembership: Int, val baseMembershipName: String, val stars: Double, val img: String) {
    fun isIncluded(membershipId: Int) = this.baseMembership <= membershipId
}

data class PairHour(val  openingHour: Int, val  openingMinute: Int, val  closingHour: Int, val  closingMinute: Int)

data class Tags(val memberships: List<Membership>, val tags: Map<String, List<String>>) //tags are title to tags

data class Location(val x: Double, val y: Double)

class RestaurantViewModel : ViewModel() {
    lateinit var restaurant: Restaurant

    suspend fun get(id: String) {
        RestaurantService.getRestaurant(id).let {
            this.restaurant = it
        }
    }
}

