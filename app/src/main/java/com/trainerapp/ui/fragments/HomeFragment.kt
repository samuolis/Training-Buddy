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
                eventViewModel.loadDetailsEvent()
                (activity as NavigationActivity).showEventCreateDialogFragment()
            }
            home_recyclerview.layoutManager = LinearLayoutManager(context)
            swipe_container.setOnRefreshListener {
                eventViewModel.loadEvents()
            }
            swipe_container.setColorSchemeResources(R.color.colorAccent)
            loadUi()

            expired_event_layout.setOnClickListener {
                (activity as NavigationActivity).showArchivedEventsDialogFragment()
            }
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
                            eventViewModel.loadDetailsEvent(it[position].eventId)
                            (activity as NavigationActivity).showEventDetailsDialogFragment()
                        }
                    }
            )
        })
        eventViewModel.getArchivedEvents()?.observe(this, Observer {
            if (it != null) {
                expired_event_count.text = it.size.toString()
                expired_event_layout.visibility = View.VISIBLE
            }
        })
        eventViewModel.getStatus()?.observe(this, Observer {
            swipe_container.isRefreshing = !(it == 0)
        })
    }
}
