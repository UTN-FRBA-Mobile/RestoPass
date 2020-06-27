package com.example.restopass.main.common.restaurant

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.common.orElse
import com.example.restopass.domain.*
import com.example.restopass.main.MainActivity
import com.example.restopass.main.common.AlertDialog
import com.example.restopass.service.RestaurantScore
import com.example.restopass.service.RestaurantService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_rating_start.*
import kotlinx.android.synthetic.main.fragment_restaurant.dishRecyclerV
import kotlinx.android.synthetic.main.fragment_restaurant.restaurantAddress
import kotlinx.android.synthetic.main.fragment_restaurant.restaurantImage
import kotlinx.android.synthetic.main.fragment_restaurant.restaurantName
import kotlinx.android.synthetic.main.fragment_restaurant.restaurantScrollView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class RestaurantRatingFragment : Fragment() {

    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var dishAdapter: DishAdapterRating

    private lateinit var viewModel: MembershipsViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel

    private lateinit var restaurant: Restaurant

    private lateinit var selectedDish: Dish

    private val rating: MutableLiveData<Rating> = MutableLiveData(Rating())

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rating_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restaurantScrollView.visibility = View.GONE
        loader.visibility = View.VISIBLE
        rateFloatingButton.visibility = View.GONE

        goToFirstStep()

        changeDish.setOnClickListener {
            goToFirstStep()
        }

        restoRatingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { p0: RatingBar, p1: Float, p2: Boolean ->
            rating.value = rating.value?.copy(resto = p1.toInt())
        }

        dishRatingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { p0: RatingBar, p1: Float, p2: Boolean ->
            rating.value = rating.value?.copy(dish = p1.toInt())
        }

        viewModel = ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)
        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        rating.observe(viewLifecycleOwner, Observer<Rating> { newRating ->
            if (newRating.dish > 0 && newRating.resto > 0) {
                rateFloatingButton.visibility = View.VISIBLE
            }
        })

        rateFloatingButton.setOnClickListener {
            restaurantScrollView.visibility = View.GONE
            rateFloatingButton.visibility = View.GONE
            loader.visibility = View.VISIBLE
            rating.value?.let { (activity as MainActivity).scoreRestaurant(it, restaurantId = restaurant.restaurantId, dishId = selectedDish.dishId) }


            AlertDialog.getAlertDialog(
                context,
                layoutInflater.inflate(R.layout.thanks_score, container, false), withButton = false
            ).show()
            Handler().postDelayed({
                view.findNavController().navigate(R.id.navigation_enrolled_home)
                val intent = Intent(this.context, MainActivity::class.java)
                startActivity(intent)
            }, 1500)
        }

        //getRestaurant("b200dcd7-dabd-4df2-9305-edaf90dad56b")
        arguments?.getString("restaurantId")?.let {
            getRestaurant(it)
        }.orElse {
            Toast.makeText(this.context, "RestaurantId not found", Toast.LENGTH_LONG).show()
        }
    }

    private fun fillView(restaurant: Restaurant) {

        val dpCalculation: Float = resources.displayMetrics.density

        restaurant.let {
            Glide.with(this).load(it.img).into(restaurantImage)
            restaurantName.text = it.name
            restaurantAddress.text = it.address
        }

        AppPreferences.user.actualMembership?.let {
            val filteredDishes = restaurant.dishes.filter {dish ->
                dish.isIncluded(it)
            }
            dishAdapter = DishAdapterRating(filteredDishes, dpCalculation, this)
        }

        dishAdapter.notifyDataSetChanged()
        dishRecyclerView = dishRecyclerV.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dishAdapter
        }

        rateRestoText.text = resources.getString(R.string.visit_rating, restaurant.name)
    }

    private fun getRestaurant(id: String) {
        coroutineScope.launch {
            try {
                restaurant = RestaurantService.getRestaurant(id)
                fillView(restaurant)
                restaurantScrollView.visibility = View.VISIBLE
                loader.visibility = View.GONE
            } catch (e: Exception) {
                Timber.i("Error while getting restaurant for id ${id}. Err: ${e.message}")
            }
        }
    }

    fun onDishSelected(dish: Dish) {
        selectedDish = dish
        dishNameText.text = dish.name
        goToSecondStep()
    }

    private fun goToFirstStep() {
        ratingFirstStep.visibility = View.VISIBLE
        ratingSecondStep.visibility = View.GONE
        rateFloatingButton.visibility = View.GONE
        resetRating()
        rating.value = Rating()
    }

    private fun resetRating() {
        restoRatingBar.rating = 0f
        dishRatingBar.rating = 0f
    }

    private fun goToSecondStep() {
        ratingFirstStep.visibility = View.GONE
        ratingSecondStep.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}

data class Rating(val dish: Int = 0, val resto: Int = 0)