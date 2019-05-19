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
import kotlinx.android.synthetic.main.fragment_dashboard.*


class DashboardFragment : Fragment() {

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboard_recyclerview.layoutManager = LinearLayoutManager(context)
        eventViewModel.getEventsOfLocation()?.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                dashboard_recyclerview.adapter = UserEventsRecyclerViewAdapter(
                        it,
                        context!!
                ) { position ->
                    (activity as NavigationActivity)
                            .showEventDetailsDialogFragment(it[position].eventId!!)
                }
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
}
