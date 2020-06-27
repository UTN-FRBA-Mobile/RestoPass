package com.example.restopass.main.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.restopass.R
import com.example.restopass.domain.Restaurant
import com.example.restopass.domain.RestaurantViewModel
import com.example.restopass.main.common.LocationService
import com.example.restopass.service.RestaurantService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.*
import timber.log.Timber


class MapFragment : Fragment(), OnMapReadyCallback{

    private lateinit var mapViewModel: MapViewModel
    private lateinit var restaurantModelView: RestaurantViewModel
    private lateinit var mMap: GoogleMap
    private var location: LatLng? = null
    val job = Job()
    val coroutineScope = CoroutineScope(job + Dispatchers.Main)
    private var currentRestaurants: List<Restaurant> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapViewModel =
            ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        restaurantModelView =
            ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)
        fetchFilters(mapViewModel)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchHereButton.visibility = View.GONE
        searchHereButton.setOnClickListener {
            search(mMap.cameraPosition.target)
            searchHereButton.visibility = View.GONE
        }
        val mapFragment =  childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapSearch.setEndIconOnClickListener {
            view.findNavController().navigate(R.id.filterFragment)
        }

        restaurantPreview.setOnClickListener {
            view.findNavController().navigate(R.id.restaurantFragment)
        }

        mapSearchEdit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mapViewModel.selectedFilters = mapViewModel.selectedFilters.copy(search = v.text.toString())
                search(mMap.cameraPosition.target)
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        location = null
        @SuppressLint("MissingPermission")
        if(LocationService.isLocationGranted())
            mMap.isMyLocationEnabled = true
        mMap.setOnMarkerClickListener {
            val restaurant = currentRestaurants.find { resto -> resto.name == it.title }
            restaurant?.let { fillRestaurantPreview(it) }
            true
        }
        mMap.setOnCameraMoveListener { searchHereButton.visibility = View.VISIBLE }
        positionMyLocationOnBottomRight()

        LocationService.addLocationListener {lastLocation : Location? ->
            lastLocation?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                location?:apply {
                    search(latLng)
                }
                location = latLng
            }

        }
    }

    private fun search(latLng: LatLng) {
        mMap.clear()
        moveCamera(latLng)
        getRestaurantsForTags(latLng, mapViewModel.selectedFilters)
        searchHereButton.visibility = View.GONE
    }

    private fun positionMyLocationOnBottomRight() {
        val locationButton= this.activity?.let { (it.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2")) }
        val rlp = locationButton?.layoutParams as (RelativeLayout.LayoutParams)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE)
        rlp.setMargins(0,0,30,30);
    }

    private fun getRestaurantsForTags(latLng: LatLng, selectedFilters: SelectedFilters) {
        coroutineScope.launch {
            try {
                val restaurants = RestaurantService.getRestaurantsForTags(latLng, selectedFilters)
                onRestaurantsSearched(restaurants)
            } catch (e: Exception) {
                Timber.i("Error while getting restaurants for tags: ${selectedFilters}. Err: ${e.message}")
            }
        }
    }

    private fun onRestaurantsSearched(restaurants: List<Restaurant>) {
        Timber.i("Got ${restaurants.size} restaurants")
        currentRestaurants = restaurants
        if(restaurants.isNotEmpty()) {
            restaurants.forEach {
                val position = LatLng(it.location.y, it.location.x)
                val marker = mMap.addMarker(MarkerOptions().position(position).title(it.name))
            }
            hidePreview()
        } else {
           hidePreview()
        }
    }

    private fun hidePreview() {
        restaurantPreview.visibility = View.GONE
        restoPreviewStar1.visibility = View.GONE
        restoPreviewStar2.visibility = View.GONE
        restoPreviewStar3.visibility = View.GONE
        restoPreviewStar4.visibility = View.GONE
        restoPreviewStar5.visibility = View.GONE
        restoPreviewHalfStar.visibility = View.GONE
    }

    private fun moveCamera(loc: LatLng, zoom: Float = 15f) = mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoom))

    private fun fetchFilters(mapViewModel: MapViewModel) {
        coroutineScope.launch {
            try {
                val tags = RestaurantService.getTags()
                mapViewModel.filters = Filters(tags.tags, tags.memberships)
            } catch (e: Exception) {
                Timber.i("Error while getting tags: ${e.message}")
            }
        }
    }

    private fun fillRestaurantPreview(restaurant: Restaurant) {
        restaurantModelView.restaurant = restaurant
        restaurantPreview.visibility = View.GONE
        Glide.with(this).load(restaurant.img).into(restoImage)
        restoName.text = restaurant.name
        //FILL STARS
        val stars = restaurant.stars
        repeat(stars.toInt()) { index ->
            val starId =
                resources.getIdentifier("restoPreviewStar${index + 1}", "id", requireContext().packageName)
            val star = this.requireView().findViewById<View>(starId)
            star.visibility = View.VISIBLE
        }
//            val hasHalfStar = stars.minus(stars.toInt()) == 0.5
//        if (hasHalfStar) restoPreviewHalfStar.visibility = View.VISIBLE
        restaurantPreview.visibility = View.VISIBLE
    }
}
