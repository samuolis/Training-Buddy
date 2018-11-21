package com.example.lukas.trainerapp.ui.fragments


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.viewmodel.EventViewModel
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.google.android.material.snackbar.Snackbar
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
                eventViewModel.loadOneEvent()
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
            var list = it
            home_recyclerview.adapter = UserEventsRecyclerViewAdapter(
                    list,
                    context!!,
                    object : UserEventsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            eventViewModel.loadOneEvent(position)
                            (activity as NavigationActivity).showEventCreateDialogFragment()
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
