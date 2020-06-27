package com.example.restopass.main.common.restaurant.restaurantsList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.style
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Restaurant
import kotlinx.android.synthetic.main.home_restaurant_item.view.*
import kotlinx.android.synthetic.main.view_restaurant_item.view.*
import kotlinx.android.synthetic.main.view_restaurant_item.view.restaurantAddress
import kotlinx.android.synthetic.main.view_restaurant_item.view.restaurantDishes
import kotlinx.android.synthetic.main.view_restaurant_item.view.restaurantImage
import kotlinx.android.synthetic.main.view_restaurant_item.view.restaurantName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RestaurantAdapter(private val listener: RestaurantAdapterListener) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    var restaurants: List<Restaurant> = listOf()

    val job = Job()
    val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    class RestaurantViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount() = restaurants.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val layout = if (listener is RestaurantsListFragment) R.layout.view_restaurant_item
        else R.layout.home_restaurant_item

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return RestaurantViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]

        holder.itemView.apply {
            Glide.with(this).load(restaurant.img).into(restaurantImage)
            restaurantName.text = restaurant.name
            restaurantAddress.text = restaurant.address
            val dishesText =
                if (restaurant.dishes.size > 1) R.string.available_dishes_plural else R.string.available_dishes_singular
            restaurantDishes.text =
                resources.getString(dishesText, restaurant.dishes.size.toString())

            if (listener is RestaurantsListFragment)
            restaurantRatingHorizontalCard.rating = restaurant.stars
            else restaurantRatingVerticalCard.rating = restaurant.stars

            if (listener is RestaurantsListFragment) {
                showMoreButton.setOnClickListener {
                    coroutineScope.launch {
                        listener.onClick(restaurant)
                    }
                }
            } else {
                this.setOnClickListener {
                    coroutineScope.launch {
                        listener.onClick(restaurant = restaurant)
                    }
                }
                if (position == restaurants.size - 1) {
                    restaurantCard.style(R.style.restaurantVerticalLastCard)
                }

            }
        }
    }
}

interface RestaurantAdapterListener {
    suspend fun onClick(restaurant: Restaurant)
}