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
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_archived_events_dialog.*

class ArchivedEventsDIalogFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_archived_events_dialog, container, false)
        var eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        rootView.post {
            archived_events_swipe_container.setOnRefreshListener {
                eventViewModel.loadEvents()
            }
            archived_events_swipe_container.setColorSchemeResources(R.color.colorAccent)
            archived_events_recyclerview.layoutManager = LinearLayoutManager(context)
            eventViewModel.getArchivedEvents()?.observe(this, Observer {
                archived_events_recyclerview.adapter = null
                var list = it
                archived_events_recyclerview.adapter = UserEventsRecyclerViewAdapter(
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

            eventViewModel.getStatus()?.observe(this, Observer {
                archived_events_swipe_container.isRefreshing = !(it == 0)
            })
        }
        return rootView
    }


}
