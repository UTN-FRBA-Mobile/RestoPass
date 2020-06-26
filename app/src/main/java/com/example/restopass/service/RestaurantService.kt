package com.example.restopass.service

import com.example.restopass.common.error
import com.example.restopass.connection.RetrofitFactory
import com.example.restopass.domain.Restaurant
import com.example.restopass.domain.Tags
import com.example.restopass.main.ui.map.SelectedFilters
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*
import timber.log.Timber


object RestaurantService {
    private var api: RestopassApi = RetrofitFactory.createClient(BASE_URL, RestopassApi::class.java)

    interface RestopassApi {
        @PUT("restaurants")
        fun getRestaurantForTagsAsync(@Body tags: TagsRequestBody):
                Deferred<Response<List<Restaurant>>>

        @GET("restaurants/tags")
        fun getRestaurantTagsAsync(): Deferred<Response<Tags>>

        @GET("restaurants/{id}")
        fun getRestaurantAsync(@Path("id") id: String): Deferred<Response<Restaurant>>

        @GET("restaurants/favorites")
        fun getFavoritesRestaurantsAsync(): Deferred<Response<List<Restaurant>>>

        @POST("restaurants/score")
        fun scoreRestaurantAndDishAsync(@Body score: RestaurantScore): Deferred<Response<Unit>>
    }

    suspend fun getRestaurants(latLng: LatLng): List<Restaurant> {
        val response = api.getRestaurantForTagsAsync(TagsRequestBody(lat = latLng.latitude, lng = latLng.longitude)).await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }

    suspend fun getRestaurantsForTags(latLng: LatLng, selectedFilters: SelectedFilters): List<Restaurant> {
        val response = api.getRestaurantForTagsAsync(TagsRequestBody(selectedFilters.search, selectedFilters.tags, selectedFilters.plan, lat = latLng.latitude, lng = latLng.longitude)).await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }

    suspend fun getTags(): Tags {
        val response = api.getRestaurantTagsAsync().await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }

    suspend fun getRestaurant(id: String): Restaurant {
        val response = api.getRestaurantAsync(id).await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }

    suspend fun getFavoriteRestaurants(): List<Restaurant> {
        val response = api.getFavoritesRestaurantsAsync().await()
        Timber.i("Executed GET to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }

    suspend fun scoreRestaurant(score: RestaurantScore): Unit {
        val response = api.scoreRestaurantAndDishAsync(score).await()
        Timber.i("Executed POST to ${response.raw()}. Response code was ${response.code()}")
        return when {
            response.isSuccessful -> response.body()!!
            else -> throw response.error()
        }
    }
}

data class TagsRequestBody(val freeText: String? = null,
                           val tags: List<String> = listOf(),
                           val topMembership: Int? = null,
                           val lat: Double, val lng: Double)

data class RestaurantScore(val restaurantId: String, val dishId: String, val starsRestaurant: Int, val starsDish: Int)