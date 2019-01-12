package com.trainerapp.ui.fragments


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.trainerapp.R
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_home, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        rootView.post {
            fab.setOnClickListener {
                eventViewModel.loadDetailsOneEvent()
                (activity as NavigationActivity).showEventCreateDialogFragment()
            }
            home_recyclerview.layoutManager = LinearLayoutManager(context)
            swipe_container.setOnRefreshListener {
                eventViewModel.loadEvents()
            }
            swipe_container.setColorSchemeResources(R.color.colorAccent)
            loadUi()
        }
        return rootView
    }

    fun loadUi(){
        eventViewModel.getEvents()?.observe(this, Observer {
            home_recyclerview.adapter = null
            var list = it
            home_recyclerview.adapter = UserEventsRecyclerViewAdapter(
                    list,
                    context!!,
                    object : UserEventsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            eventViewModel.loadOneEventInHome(position)
                            (activity as NavigationActivity).showEventDetailsDialogFragment()
                        }
                    }
            )
        })
        eventViewModel.getStatus()?.observe(this, Observer {
            swipe_container.isRefreshing = !(it == 0)
        })
    }

    override fun onResume() {
        super.onResume()
    }
}