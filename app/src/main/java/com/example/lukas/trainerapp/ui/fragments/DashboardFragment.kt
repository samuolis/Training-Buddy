package com.example.lukas.trainerapp.ui.fragments


import android.Manifest
import android.content.Context
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lukas.trainerapp.db.entity.Event
import com.example.lukas.trainerapp.db.viewmodel.EventViewModel
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.example.lukas.trainerapp.webService.EventWebService
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*


    class DashboardFragment : Fragment() {

        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private var userLocation: Location? = null
        private var userLocationCountryCode: String? = null
    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val sharedPref = context?.getSharedPreferences(context?.getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        rootView.post {
            getDataFromLocation { gotLocation, countryCode ->
                userLocation = gotLocation
                userLocationCountryCode = countryCode
                dashboard_recyclerview.layoutManager = LinearLayoutManager(context)
                eventViewModel.getEventsOfLocation(userLocationCountryCode,
                        userLocation?.latitude?.toFloat(), userLocation?.longitude?.toFloat())?.observe(this, androidx.lifecycle.Observer {
                    dashboard_recyclerview.adapter = UserEventsRecyclerViewAdapter(it, context!!, null)
                })
                eventViewModel.getStatus()?.observe(this, Observer {
                    dashboard_swipe_container.isRefreshing = !(it == 0)
                })
                dashboard_swipe_container.setOnRefreshListener {
                    eventViewModel.loadEventsByLocation(userLocationCountryCode,
                            userLocation?.latitude?.toFloat(), userLocation?.longitude?.toFloat())
                }
            }
            dashboard_fab.setOnClickListener {
                (activity as NavigationActivity).showDashnoardSearchDialogFragment()
            }
            dashboard_swipe_container.setColorSchemeResources(R.color.colorAccent)
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
