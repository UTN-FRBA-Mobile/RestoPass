package com.example.restopass.main.ui.home

import androidx.lifecycle.ViewModel
import com.example.restopass.domain.Membership
import com.example.restopass.domain.Restaurant
import com.example.restopass.service.MembershipService
import com.example.restopass.service.RestaurantService
import com.google.android.gms.maps.model.LatLng

class HomeViewModel : ViewModel() {
    lateinit var memberships: List<Membership>
    lateinit var restaurants: List<Restaurant>

    suspend fun getMemberships() {
        MembershipService.getMemberships().let {
            this.memberships = it.memberships
        }
    }

    suspend fun getRestaurants(latLng: LatLng) {
        RestaurantService.getRestaurants(latLng).let {
            this.restaurants = it
        }
    }
}