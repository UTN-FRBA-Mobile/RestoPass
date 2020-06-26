package com.example.restopass.main.common.restaurant.restaurantsList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.common.AppPreferences
import com.example.restopass.domain.*
import com.example.restopass.main.common.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_restaurants_list.*
import kotlinx.coroutines.*
import timber.log.Timber

class RestaurantsListFragment : Fragment(), RestaurantAdapterListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
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
        return inflater.inflate(R.layout.fragment_restaurants_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MembershipsViewModel::class.java)
        restaurantViewModel = ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        selectedMembership = ViewModelProvider(requireActivity()).get(SelectedMembershipViewModel::class.java)


        restaurantAdapter =
            RestaurantAdapter(
                this
            )
        restaurantAdapter.restaurants = selectedMembership.membership?.restaurants!!
        restaurantAdapter.notifyDataSetChanged()

        recyclerView = restaurantRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = restaurantAdapter
        }

        AppPreferences.user.actualMembership?.let {
            if (it == selectedMembership.membership!!.membershipId) {
               floatingButtton.visibility = View.GONE
            } else {
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0)
                            floatingButtton.hide()
                        else if (dy < 0)
                            floatingButtton.show()
                    }
                })
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (job.isCancelled)  {
            job = Job()
            coroutineScope = CoroutineScope(job + Dispatchers.Main)
        }
    }

    override suspend fun onClick(restaurant: Restaurant) {
        withContext(coroutineScope.coroutineContext) {
            try {
                restaurantsList.visibility = View.GONE
                notEnrolledLoader.visibility = View.VISIBLE
                restaurantViewModel.get(restaurant.restaurantId)
            } catch (e: Exception) {
                if(isActive) {
                    Timber.e(e)
                    notEnrolledLoader.visibility = View.GONE
                    restaurantsList.visibility = View.VISIBLE

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


        findNavController().navigate(R.id.restaurantFragment,  bundleOf("isMembershipSelected" to true))
    }



    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}