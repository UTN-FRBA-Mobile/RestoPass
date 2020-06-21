package com.example.restopass.main.common.restaurant

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.common.orElse
import com.example.restopass.domain.*
import com.example.restopass.service.UserService
import kotlinx.android.synthetic.main.fragment_restaurant.*
import kotlinx.coroutines.*
import timber.log.Timber

class RestaurantFragment : Fragment() {
    private lateinit var tagRecyclerView: RecyclerView
    private lateinit var tagAdapter: TagAdapter

    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var dishAdapter: DishAdapter

    private lateinit var viewModel: MembershipsViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel

    private lateinit var selectedMembership: SelectedMembershipViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restaurant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)
        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        selectedMembership =
            ViewModelProvider(requireActivity()).get(SelectedMembershipViewModel::class.java)

        val restaurant = restaurantViewModel.restaurant


        tagAdapter = TagAdapter()

        tagAdapter.tags = restaurant.tags
        tagAdapter.notifyDataSetChanged()

        tagRecyclerView = tagRestaurantRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tagAdapter
        }

        val isMembershipSelected = arguments?.getBoolean("isMembershipSelected")

        // Si viene de una tarjeta Membresía, mostramos los platos en el órden de inclusión de esa membresía
        isMembershipSelected?.let {
            selectedMembership.membership?.apply {
                val sortedRestaurants = restaurant.dishes.sortedBy {
                    !it.isIncluded(this.membershipId!!)
                }
                dishAdapter = DishAdapter(sortedRestaurants)
                dishAdapter.selectedMembership = this
            }
        }.orElse {
            dishAdapter = DishAdapter(restaurant.dishes)
        }


        dishAdapter.notifyDataSetChanged()
        dishRecyclerView = dishRecyclerV.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dishAdapter
        }

        val selectedMembership =
            isMembershipSelected?.run { selectedMembership.membership }
        fillView(restaurant, selectedMembership)

    }

    private fun fillView(restaurant: Restaurant, selectedMembership: Membership?) {
        AppPreferences.user.favoriteRestaurants?.let {
            if (it.contains(restaurant.restaurantId)) {
               changeFavoriteIcon(R.drawable.ic_favorite_full)
            } else {
               changeFavoriteIcon(R.drawable.ic_favorite_empty)
            }
        }

        favoriteButton.setOnClickListener {
            toggleFavorite(restaurant)
        }

        restaurant.let {
            Glide.with(this).load(it.img).into(restaurantImage)
            restaurantName.text = it.name
            restaurantAddress.text = it.address
        }

        val stars = restaurant.stars
        repeat(stars.toInt()) { index ->
            val starId =
                resources.getIdentifier("star${index + 1}", "id", requireContext().packageName)
            requireView().findViewById<View>(starId).visibility = View.VISIBLE
        }
        val hasHalfStar = stars.minus(stars.toInt()) == 0.5
        if (hasHalfStar) halfStar.visibility = View.VISIBLE

        //Si tiene membresía, viene de una tarjeta Membresía y es la suya => se le muestra "Reservar Mesa"
        // Si tiene membresía, NO viene de una tarjeta Membresía y el restaurant está en su membresía => se le muestra "Reservar Mesa"
        // Decidimos no mostrar "Reservar Mesa" si viene de una tarjeta Membresía, "detalles" porque ese resto puede tener
        // varios platos que no están en su membresía y presta a confusión
        AppPreferences.user.actualMembership?.let {
            if (isActualMembership(it, selectedMembership) ||
                (selectedMembership == null && isRestaurantInMembership(it, restaurant))) {
                restaurantFloatingButton.setText(R.string.bookTable)
                restaurantFloatingButton.setOnClickListener{it.findNavController().navigate(R.id.reservationCreateStep1)}
            } else setButtonByMembership(selectedMembership?.name)
        }.orElse {
            setButtonByMembership(selectedMembership?.name)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            restaurantScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (scrollY > oldScrollY) {
                    restaurantFloatingButton.hide()
                } else {
                    restaurantFloatingButton.show()
                }
            }
        }
    }

    private fun toggleFavorite(restaurant: Restaurant) {
        AppPreferences.user.favoriteRestaurants?.let {
            if (it.contains(restaurant.restaurantId)) {
               unfavorite(restaurant)
            } else {
                favorite(restaurant)
            }
        }.orElse {
            favorite(restaurant)
        }
    }

    private fun unfavorite(restaurant: Restaurant) {
        changeFavoriteIcon(R.drawable.ic_favorite_empty)
        coroutineScope.launch {
            try {
                UserService.unfavorite(restaurant.restaurantId)
                AppPreferences.user.apply {
                    val restaurants = this.favoriteRestaurants
                    restaurants?.remove(restaurant.restaurantId)
                    AppPreferences.user = this.copy(favoriteRestaurants = restaurants)
                }
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    changeFavoriteIcon(R.drawable.ic_favorite_full)
                }
            }
        }
    }

    private fun favorite(restaurant: Restaurant) {
        changeFavoriteIcon(R.drawable.ic_favorite_full)
        coroutineScope.launch {
            try {
                UserService.favorite(restaurant.restaurantId)
                AppPreferences.user.apply {
                    var restaurants = this.favoriteRestaurants
                    restaurants?.add(restaurant.restaurantId).orElse { restaurants = mutableListOf(restaurant.restaurantId)  }
                    AppPreferences.user = this.copy(favoriteRestaurants = restaurants)
                }
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    changeFavoriteIcon(R.drawable.ic_favorite_empty)
                }
            }
        }
    }

    private fun changeFavoriteIcon(drawable: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            favoriteButton.setImageDrawable(requireContext().getDrawable(drawable))
            favoriteButton.setColorFilter(requireContext().getColor(R.color.restoPassGreen))
        } else {
            Glide.with(this).load(drawable).into(favoriteButton)
        }
    }

    private fun isRestaurantInMembership(membershipId: Int, restaurant: Restaurant): Boolean {
       return restaurant.dishes.any {
            it.isIncluded(membershipId)
        }
    }

    private fun isActualMembership(actualMembershipId: Int, selectedMembership: Membership?) : Boolean {
        return actualMembershipId == selectedMembership?.membershipId
    }

    // Si viene de una tarjeta membresía => le mostramos un "Elegir Membresía X"
    //Sino => Ver membresías
    private fun setButtonByMembership(membershipName: String?) {
        membershipName?.let {
            val chooseMembership = resources.getString(R.string.chooseMembership, membershipName)
            restaurantFloatingButton.text = chooseMembership
        }.orElse {
            restaurantFloatingButton.apply {
                setText(R.string.showMemberships)
                setOnClickListener {
                    findNavController().navigate(R.id.membershipsFragment)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}