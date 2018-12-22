package com.trainerapp.ui.fragments


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

import com.trainerapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_dashboard.*
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
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        rootView.post {
            dashboard_recyclerview.layoutManager = LinearLayoutManager(context)
            eventViewModel.getEventsOfLocation()?.observe(this, androidx.lifecycle.Observer {
                if (it != null && it.size != 0) {
                    dashboard_recyclerview.adapter = UserEventsRecyclerViewAdapter(it, context!!,
                            object : UserEventsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            eventViewModel.loadOneEventInDashboard(position)
                            (activity as NavigationActivity).showEventDetailsDialogFragment()
                        }

                    })
                }
            })
            eventViewModel.getStatus()?.observe(this, Observer {
                dashboard_swipe_container.isRefreshing = !(it == 0)
            })
            dashboard_swipe_container.setOnRefreshListener {
                eventViewModel.loadEventsByLocation()
            }

            dashboard_fab.setOnClickListener {
                (activity as NavigationActivity).showDashnoardSearchDialogFragment()
            }
            dashboard_swipe_container.setColorSchemeResources(R.color.colorAccent)
        }
        return rootView
    }

}
