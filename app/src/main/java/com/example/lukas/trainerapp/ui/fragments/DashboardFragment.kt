package com.example.lukas.trainerapp.ui.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.example.lukas.trainerapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import java.util.*


class DashboardFragment : Fragment() {

    private val REQUEST_PERMISION_CODE = 1111
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private var userLocationCountryCode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        getDataFromLocation { gotLocation, countryCode ->
            userLocation = gotLocation
            userLocationCountryCode = countryCode
        }
        return rootView
    }

    fun getDataFromLocation (callback: (gotLocation: Location?, countryCode: String?) -> Unit){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        if (ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null)
                        {
                            var geocoder = Geocoder(context, Locale.getDefault())
                            var adresses = geocoder.getFromLocation(location.latitude,
                                    location.longitude, 1)
                            var address = adresses[0]
                            callback(location, address.countryCode)
                        }
                    }
        } else {
            Toast.makeText(context, "You do not enabled location", Toast.LENGTH_LONG).show()
            callback(null, null)
        }
    }


}
