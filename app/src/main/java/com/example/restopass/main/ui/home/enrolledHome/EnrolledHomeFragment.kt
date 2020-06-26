package com.example.restopass.main.ui.home.enrolledHome

import android.location.Location
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
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.common.EmojisHelper
import com.example.restopass.domain.MembershipsViewModel
import com.example.restopass.domain.Restaurant
import com.example.restopass.domain.RestaurantViewModel
import com.example.restopass.main.common.AlertDialog
import com.example.restopass.main.common.LocationService
import com.example.restopass.main.common.restaurant.restaurantsList.RestaurantAdapter
import com.example.restopass.main.common.restaurant.restaurantsList.RestaurantAdapterListener
import com.example.restopass.main.ui.home.HomeViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_enrolled_home.*
import kotlinx.coroutines.*
import timber.log.Timber

class EnrolledHomeFragment : Fragment(), RestaurantAdapterListener {
    private lateinit var closeRestaurantRecyclerView: RecyclerView
    private lateinit var closeRestaurantAdapter: RestaurantAdapter
    private lateinit var selectedRestaurantViewModel: RestaurantViewModel

    private lateinit var favoriteRestaurantRecyclerView: RecyclerView
    private lateinit var favoriteRestaurantAdapter: RestaurantAdapter

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var membershipsViewModel: MembershipsViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enrolled_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        membershipsViewModel =
            ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)

        selectedRestaurantViewModel = ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        closeRestaurantAdapter = RestaurantAdapter(this)
        closeRestaurantRecyclerView = enrolledCloseRestaurantRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = closeRestaurantAdapter
        }

        favoriteRestaurantAdapter = RestaurantAdapter(this)
        favoriteRestaurantRecyclerView = favoriteRestaurantsRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = favoriteRestaurantAdapter
        }

        aboutButton.apply {
            aboutButtonIcon.text = EmojisHelper.leftHand
            performClick()
            setOnClickListener {
                AlertDialog.getAboutRestoPassModal(context, layoutInflater, container)
            }
        }

    }

    override fun onStart() {
        super.onStart()

        if (job.isCancelled) {
            job = Job()
            coroutineScope = CoroutineScope(job + Dispatchers.Main)
        }

        enrolledLoader.visibility = View.VISIBLE

        coroutineScope.launch {
            val deferred = mutableListOf<Deferred<Unit>>()
            if (!AppPreferences.user.favoriteRestaurants.isNullOrEmpty()) {
                deferred.add(getFavoriteRestaurants())
            }
            if (homeViewModel.restaurants == null && LocationService.isLocationGranted()) {
                deferred.add(getRestaurantsByLocation())
            }

            deferred.awaitAll()

            if (homeViewModel.restaurants != null) {
                closeRestaurantAdapter.restaurants = homeViewModel.restaurants!!
                closeRestaurantAdapter.notifyDataSetChanged()

                closeRestaurantRecyclerView.visibility = View.VISIBLE
                closeRestaurantSection.visibility = View.VISIBLE
            }

            enrolledLoader.visibility = View.GONE

            if (membershipsViewModel.wasEnrolled) {
                AlertDialog.getWelcomeMembershipModal(context, layoutInflater, container, resources, membershipsViewModel.actualMembership!!)
                membershipsViewModel.wasEnrolled = false
            }

        }
    }

    private fun getRestaurantsByLocation(): Deferred<Unit> {
        return coroutineScope.async {
            LocationService.addLocationListener { lastLocation: Location? ->
                coroutineScope.launch {
                    try {
                        homeViewModel.getRestaurants(
                            LatLng(
                                lastLocation!!.latitude,
                                lastLocation.longitude
                            )
                        )

                        closeRestaurantAdapter.restaurants = homeViewModel.restaurants!!
                        closeRestaurantAdapter.notifyDataSetChanged()

                        closeRestaurantRecyclerView.visibility = View.VISIBLE
                        closeRestaurantSection.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        if (isActive) {
                            Timber.e(e)
                            view?.findNavController()?.navigate(R.id.refreshErrorFragment)
                        }
                    }
                }
            }
        }
    }


    private fun getFavoriteRestaurants(): Deferred<Unit> {
        return coroutineScope.async {
            try {
                homeViewModel.getFavoriteRestaurants()

                favoriteRestaurantAdapter.restaurants = homeViewModel.favoriteRestaurants!!
                favoriteRestaurantAdapter.notifyDataSetChanged()

                favoriteRestaurantsSection.visibility = View.VISIBLE

            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e)
                    view?.findNavController()?.navigate(R.id.refreshErrorFragment)
                }
            }
        }

    }

    override suspend fun onClick(restaurant: Restaurant) {
        withContext(coroutineScope.coroutineContext) {
            try {
                enrolledLoader.visibility = View.VISIBLE
                selectedRestaurantViewModel.get(restaurant.restaurantId)

            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e)
                    enrolledLoader.visibility = View.GONE

                    val titleView: View =
                        layoutInflater.inflate(R.layout.alert_dialog_title, container, false)

                    AlertDialog.getAlertDialog(
                        context,
                        titleView,
                        view
                    ).show()
                }
            }
        }

        findNavController().navigate(R.id.restaurantFragment)
    }


    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}