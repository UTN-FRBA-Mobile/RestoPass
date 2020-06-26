package com.example.restopass.main.ui.home

import androidx.lifecycle.ViewModel
import com.example.restopass.domain.Restaurant
import com.example.restopass.service.RestaurantService
import com.google.android.gms.maps.model.LatLng

class HomeViewModel : ViewModel() {
    var restaurants: List<Restaurant>? = null
    lateinit var favoriteRestaurants: List<Restaurant>
    var wasEnrolled = false

    suspend fun getRestaurants(latLng: LatLng) {
        RestaurantService.getRestaurants(latLng).let {
            this.restaurants = it
        }
    }

    suspend fun getFavoriteRestaurants() {
        RestaurantService.getFavoriteRestaurants().let {
            this.favoriteRestaurants = it
        }
    }
}