package com.example.chds.geofencing

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.nfc.Tag
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chds.R
import com.example.chds.data.LocationBubble
import com.example.chds.databinding.FragmentMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class GoogleMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val model: LocationViewModel by activityViewModels()

    private val PERMISSION_ID = 1010
    var curlocation: Location = Location("Edmonton")
        get() = field
        set(location: Location){
            field = location
        }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:", "your last last location: " + lastLocation.longitude.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val view = binding.root
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.map).onCreate(savedInstanceState)
        binding.map.onResume()
        binding.map.getMapAsync(this)

        binding.svLocation.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val address = binding.svLocation.query.toString()
                val addressList: List<Address>

                if (!address.contentEquals("")) {
                    val geocoder = Geocoder(requireContext())
                    addressList = geocoder.getFromLocationName(address, 1)
                    val curAddress = addressList.get(0)
                    val latLng = LatLng(curAddress.latitude, curAddress.longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title(address))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))

                    val location = LocationBubble("", true, latLng.latitude, latLng.longitude, 10.0)
                    model.setCurrentLocation(location)
                    if (findNavController().currentDestination?.id != R.id.saveLocationBottomSheet) {
                        findNavController().navigate(R.id.action_googleMapFragment_to_saveLocationBottomSheet)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        if (model.update){
            val locationBubble = model.updatedLocation.value
            if (locationBubble != null){
                val latLng = LatLng(locationBubble.lat, locationBubble.lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
                if (findNavController().currentDestination?.id != R.id.updateLocationBottomSheet) {
                    findNavController().navigate(R.id.action_googleMapFragment_to_updateLocationBottomSheet)
                }
            }
        }
        else{
            getLastLocation()
            setMapLongClick(mMap)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            println(latLng.latitude)
            val location = LocationBubble("", true, latLng.latitude, latLng.longitude, 10.0)
            model.setCurrentLocation(location)
            if (findNavController().currentDestination?.id != R.id.saveLocationBottomSheet) {
                findNavController().navigate(R.id.action_googleMapFragment_to_saveLocationBottomSheet)
            }
        }
    }


    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "onRequestPermissionResult")
            }
        }
    }

    private fun getLastLocation(){
        if(isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
                return
            }
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                var location: Location? = task.result
                if (location == null) {
                    newLocationData()
                } else {
                    Log.d("Debug:", "Your Location:" + location.longitude)
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
                    println("Current location")
                    Log.e("Tag", location.latitude.toString())
                }
            }
        }
        else{
            Toast.makeText(requireContext(),"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
        }
    }

    private fun newLocationData(){
        val locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }




}