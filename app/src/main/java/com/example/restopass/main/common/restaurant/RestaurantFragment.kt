package com.example.restopass.main.common.restaurant

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.common.orElse
import com.example.restopass.domain.Membership
import com.example.restopass.domain.MembershipsViewModel
import com.example.restopass.domain.Restaurant
import com.example.restopass.domain.RestaurantViewModel
import com.example.restopass.main.MainActivity
import com.example.restopass.main.common.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_restaurant.*
import kotlinx.coroutines.*
import timber.log.Timber

class RestaurantFragment : Fragment() {
    private lateinit var tagRecyclerView: RecyclerView
    private lateinit var tagAdapter: TagAdapter

    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var dishAdapter: DishAdapter

    private lateinit var membershipsViewModel: MembershipsViewModel
    private lateinit var restaurantViewModel: RestaurantViewModel


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

        membershipsViewModel = ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)
        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)


        val restaurant = restaurantViewModel.restaurant


        tagAdapter = TagAdapter()

        tagAdapter.tags = restaurant.tags
        tagAdapter.notifyDataSetChanged()

        tagRecyclerView = tagRestaurantRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tagAdapter
        }

        val isMembershipSelected = arguments?.getBoolean("isMembershipSelected")

        // Si viene de una Membership Card, mostramos los platos en el órden de esa membresía.
        // Si viene de otro lado y está enrolado, mostramos los platos en el órden de inclusión de su membresía.
        // En cualquier otro caso, mostramos los restaurantes por como vienen
        val sortedDishes = if (isMembershipSelected == true)  {
            restaurant.dishes.sortedBy {
                !it.isIncluded(membershipsViewModel.selectedMembership!!.membershipId!!)
            }
        } else {
            AppPreferences.user.actualMembership?.run {
                restaurant.dishes.sortedBy {
                    !it.isIncluded(this)
                }
            }
        }

        dishAdapter = DishAdapter(sortedDishes ?: restaurant.dishes )
        dishAdapter.notifyDataSetChanged()
        if (isMembershipSelected == true ) {
            dishAdapter.selectedMembership = membershipsViewModel.selectedMembership
        }

        dishRecyclerView = dishRecyclerV.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dishAdapter
        }

        val selectedMembership = isMembershipSelected?.run { membershipsViewModel.selectedMembership }
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

        restaurantRating.rating = restaurant.stars


        //Si tiene membresía, viene de una tarjeta Membresía y es la suya => se le muestra "Reservar Mesa"
        // Si tiene membresía, NO viene de una tarjeta Membresía y el restaurant está en su membresía => se le muestra "Reservar Mesa"
        // Decidimos no mostrar "Reservar Mesa" si viene de una tarjeta Membresía, "detalles" porque ese resto puede tener
        // varios platos que no están en su membresía y presta a confusión
        AppPreferences.user.actualMembership?.let {
            if (isActualMembership(it, selectedMembership) ||
                (selectedMembership == null && isRestaurantInMembership(it, restaurant))) {
                restaurantFloatingButton.setOnClickListener{
                    if(AppPreferences.user.visits <= 0) {
                        showNoMoreVisitsDialog()
                    } else {
                        it.findNavController().navigate(R.id.reservationCreateStep1)}
                    }
                restaurantFloatingButton.setText(R.string.bookTable)
            } else setButtonByMembership(selectedMembership)
        }.orElse {
            setButtonByMembership(selectedMembership)
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
                (activity as MainActivity).unfavorite(restaurant)
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
                (activity as MainActivity).favorite(restaurant)
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

    private fun toggleLoader() {
        restaurantLoader.visibility = if (restaurantLoader.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        val visibility =  if (restaurantLoader.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        restaurantScrollView.visibility =  visibility
        restaurantFloatingButton.visibility = visibility
    }

    // Si viene de una tarjeta membresía => le mostramos un "Elegir Membresía X"
    //Sino => Ver membresías
    private fun setButtonByMembership(membership: Membership?) {
        membership?.let {
            val chooseMembership = resources.getString(R.string.chooseMembership, membership.name)
            restaurantFloatingButton.text = chooseMembership
            restaurantFloatingButton.setOnClickListener {
                toggleLoader()
                coroutineScope.launch {
                    try {
                        membershipsViewModel.update(membership)
                        findNavController().navigate(R.id.navigation_enrolled_home)
                    } catch (e: Exception) {
                        if(isActive) {
                            Timber.e(e)
                            toggleLoader()

                            val body: View =
                                layoutInflater.inflate(R.layout.alert_dialog_title, container, false)
                            AlertDialog.getAlertDialog(
                                context,
                                body,
                                view
                            ).show()
                        }
                    }
                }
            }
        }.orElse {
            restaurantFloatingButton.apply {
                setText(R.string.showMemberships)
                setOnClickListener {
                    findNavController().navigate(R.id.membershipsFragment)
                }
            }
        }
    }

    private fun showNoMoreVisitsDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        requireView().findNavController().navigate(R.id.membershipsFragment)
                    }
                }
            }
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireView().context)
        builder.setMessage(R.string.no_more_visits)
            .setPositiveButton("Ver Membresias", dialogClickListener)
            .show()
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}