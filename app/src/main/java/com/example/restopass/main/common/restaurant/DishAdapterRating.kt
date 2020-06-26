package com.example.restopass.main.common.restaurant

import com.example.restopass.domain.Dish
import kotlinx.android.synthetic.main.dish_item.view.*

class DishAdapterRating(
    private val dishes: List<Dish>,
    private val dpCalculation: Float,
    private val ratingFragment: RestaurantRatingFragment
) : DishAdapter(dishes, false) {

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.apply {
            dishCard.layoutParams.height = (250 * dpCalculation).toInt()
            dishItem.setOnClickListener {
                ratingFragment.onDishSelected(dishes[position])
            }
        }
    }

}