package com.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    private fun loadUi() {
        eventViewModel.getEvents()?.observe(this, Observer {
            home_recyclerview.adapter = null
            val list = it
            home_recyclerview.adapter = UserEventsRecyclerViewAdapter(
                    list,
                    context!!
            ) { position ->
                (activity as NavigationActivity)
                        .showEventDetailsDialogFragment(it[position].eventId!!)
            }
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
