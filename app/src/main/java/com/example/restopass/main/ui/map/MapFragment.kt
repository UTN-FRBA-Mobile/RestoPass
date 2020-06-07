package com.example.restopass.main.ui.map

import android.Manifest
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
import androidx.navigation.findNavController
import com.example.restopass.R
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
    private lateinit var mMap: GoogleMap
    private val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    private val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    private val permissions = arrayOf(fineLocation, coarseLocation)
    private val permissionCode = 1234
    private var locationGranted = false
    private var location: LatLng? = null
    val job = Job()
    val coroutineScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapViewModel =
            ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        fetchFilters(mapViewModel)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        initializeLocation()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasFilters()) search(null)
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
        mapSearchEdit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(mMap.cameraPosition.target)
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationButtonClickListener {
            search(location)
            false
        }
        mMap.setOnCameraMoveListener { searchHereButton.visibility = View.VISIBLE }
        positionMyLocationOnBottomRight()
    }

    private fun search(latLng: LatLng?) {
        mMap.clear()
        if (hasFilters()) {
            Toast.makeText(this.context, "searchWithFilter", Toast.LENGTH_SHORT).show()
            getRestaurantsForTags(mapViewModel.selectedFilters)
        } else {
            Toast.makeText(this.context, "search", Toast.LENGTH_SHORT).show()
            latLng?.let { getRestaurantsForLocation(it) }
        }
        searchHereButton.visibility = View.GONE
    }

    private fun hasFilters(): Boolean = mapViewModel.selectedFilters != SelectedFilters()


    private fun positionMyLocationOnBottomRight() {
        val locationButton= this.activity?.let { (it.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2")) }
        val rlp = locationButton?.layoutParams as (RelativeLayout.LayoutParams)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE)
        rlp.setMargins(0,0,30,30);
    }

    private fun initializeLocation() {
        locationGranted = getLocationPermissions()
        if (!locationGranted)
            requestLocationPermission()
        else
            getLocation()
    }

    private fun getRestaurantsForLocation(latLng: LatLng) {
        coroutineScope.launch {
            try {
                val restaurants = RestaurantService.getRestaurants(latLng)
                Timber.i("Got ${restaurants.size} restaurants")
                restaurants.forEach {
                    val position = LatLng(it.location.y, it.location.x)
                    mMap.addMarker(MarkerOptions().position(position))
                }
            } catch (e: Exception) {
                Timber.i("Error while getting restaurants for latLng: ${latLng}. Err: ${e.message}")
            }
        }
    }

    private fun getRestaurantsForTags(selectedFilters: SelectedFilters) {
        coroutineScope.launch {
            try {
                val restaurants = RestaurantService.getRestaurantsForTags(selectedFilters)
                Timber.i("Got ${restaurants.size} restaurants")
                restaurants.forEach {
                    val position = LatLng(it.location.y, it.location.x)
                    mMap.addMarker(MarkerOptions().position(position))
                }
            } catch (e: Exception) {
                Timber.i("Error while getting restaurants for tags: ${selectedFilters}. Err: ${e.message}")
            }
        }
    }

    private fun getLocation() {
        val fuseLoc = this.context?.let { LocationServices.getFusedLocationProviderClient(it) }
        if (locationGranted) {
            fuseLoc?.lastLocation?.addOnSuccessListener  {lastLocation : Location? ->
                lastLocation?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    location?:apply {
                        if(!hasFilters()) {
                            moveCamera(latLng)
                            search(latLng)
                        }
                    }
                    location = latLng
                }
            }
        }
    }

    private fun moveCamera(loc: LatLng, zoom: Float = 15f) = mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoom))

    private fun getLocationPermissions() = permissions.all { perm -> this.context?.let { ContextCompat.checkSelfPermission(it, perm) } == PackageManager.PERMISSION_GRANTED }

    private fun requestLocationPermission() = this.activity?.let { ActivityCompat.requestPermissions(it, permissions, permissionCode) }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults.all { it ==  PackageManager.PERMISSION_GRANTED }) {
                locationGranted = true
                getLocation()
            }
        }
    }

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

}
