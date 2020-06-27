package com.example.restopass.main.ui.home.notEnrolledHome

import android.content.Context
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
import com.example.restopass.domain.*
import com.example.restopass.main.common.AlertDialog
import com.example.restopass.main.common.LocationService
import com.example.restopass.main.common.membership.MembershipAdapter
import com.example.restopass.main.common.membership.MembershipAdapterListener
import com.example.restopass.main.common.restaurant.restaurantsList.RestaurantAdapter
import com.example.restopass.main.common.restaurant.restaurantsList.RestaurantAdapterListener
import com.example.restopass.main.ui.home.HomeViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_not_enrolled_home.*
import kotlinx.coroutines.*
import timber.log.Timber

class NotEnrolledHomeFragment : Fragment(), RestaurantAdapterListener, MembershipAdapterListener {
    private var listener: NotEnrolledFragmentListener? = null

    private lateinit var membershipRecyclerView: RecyclerView
    private lateinit var membershipAdapter: MembershipAdapter

    private lateinit var restaurantRecyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var restaurantViewModel: RestaurantViewModel

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var membershipsViewModel: MembershipsViewModel

    var job = Job()
    var coroutineScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_not_enrolled_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        membershipsViewModel =
            ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)

        restaurantViewModel =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        membershipAdapter = MembershipAdapter(this)
        membershipRecyclerView = homeMembershipRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = membershipAdapter
        }

        restaurantAdapter =
            RestaurantAdapter(
                this
            )
        restaurantRecyclerView = homeRestaurantRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = restaurantAdapter
        }

        aboutButtonNotEnrolled.apply {
            aboutButtonNotEnrolledIcon.text =  EmojisHelper.leftHand
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

        notEnrolledLoader.visibility = View.VISIBLE

        coroutineScope.launch {
            val deferred = mutableListOf(getMemberships())
            if (LocationService.isLocationGranted()) {
                deferred.add(getRestaurantsByLocation())
            }
            deferred.awaitAll()

            val isSignUp = requireActivity().intent?.getBooleanExtra("signUp", false)
            if (isSignUp == true) {
                requireActivity().intent?.removeExtra("signUp")
                AlertDialog.getAboutRestoPassModal(context, layoutInflater, container)
            }

            notEnrolledLoader.visibility = View.GONE
        }

    }


    private fun getMemberships(): Deferred<Unit> {
        return coroutineScope.async {
            try {
                membershipsViewModel.get()

                membershipAdapter.memberships = membershipsViewModel.memberships
                membershipAdapter.notifyDataSetChanged()

                membershipSection.visibility = View.VISIBLE
            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e)
                    view?.findNavController()?.navigate(R.id.refreshErrorFragment)
                }
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

                        restaurantAdapter.restaurants = homeViewModel.restaurants!!
                        restaurantAdapter.notifyDataSetChanged()

                        homeRestaurantRecycler.visibility = View.VISIBLE
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

    override fun onEnrollClick(membership: Membership) {
        notEnrolledLoader.visibility = View.VISIBLE
        coroutineScope.launch {
            try {
                membershipsViewModel.update(membership)

                AppPreferences.user.apply {
                    AppPreferences.user = this.copy(actualMembership = membership.membershipId)
                }

                membershipsViewModel.wasEnrolled = true
                listener?.onEnrollClick()
            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e)
                    notEnrolledLoader.visibility = View.GONE

                    view?.findNavController()?.navigate(R.id.refreshErrorFragment)
                }
            }
        }
    }


    override suspend fun onClick(restaurant: Restaurant) {
        withContext(coroutineScope.coroutineContext) {
            try {
                notEnrolledLoader.visibility = View.VISIBLE
                restaurantViewModel.get(restaurant.restaurantId)

            } catch (e: Exception) {
                if (isActive) {
                    Timber.e(e)
                    notEnrolledLoader.visibility = View.GONE

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


    override fun onDetailsClick(membership: Membership) {
        membershipsViewModel.selectedMembership = membership
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NotEnrolledFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}

interface NotEnrolledFragmentListener {
    fun onEnrollClick()
}
