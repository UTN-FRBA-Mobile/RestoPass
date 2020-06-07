package com.example.restopass.main.common.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Restaurant
import kotlinx.android.synthetic.main.view_restaurant_item.view.*

class RestaurantAdapter(private val from: Fragment) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    var restaurants: List<Restaurant> = listOf()

    class RestaurantViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount() = restaurants.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val layout = if (from is RestaurantsFragment) R.layout.view_restaurant_item
        else R.layout.home_restaurant_item

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return RestaurantViewHolder(view)
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

            val stars = restaurant.stars
            repeat(stars.toInt()) { index ->
                val starId =
                    resources.getIdentifier("star${index + 1}", "id", context.packageName)
                findViewById<View>(starId).visibility = View.VISIBLE
            }
            val hasHalfStar = stars.minus(stars.toInt()) == 0.5
            if (hasHalfStar) halfStar.visibility = View.VISIBLE
        }
    }
}