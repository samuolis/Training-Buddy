package com.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.enums.EventDetailScreen
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import javax.inject.Inject


class DashboardFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventViewModel = getViewModel(viewModelFactory)
        dashboard_recyclerview.layoutManager = LinearLayoutManager(context)
        eventViewModel.eventsByLocation.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                dashboard_recyclerview.adapter = UserEventsRecyclerViewAdapter(
                        it,
                        context!!
                ) { position ->
                    (activity as NavigationActivity)
                            .showEventDetailsDialogFragment(
                                    it[position].eventId!!,
                                    EventDetailScreen.DASHBOARD
                            )
                }
            }
        })
        eventViewModel.refreshStatus.observe(this, Observer {
            dashboard_swipe_container.isRefreshing = !(it == 0)
        })
        eventViewModel.errorData.nonNullObserve(this) {
            Toast.makeText(context,
                    "Failed to get data " + it.localizedMessage,
                    Toast.LENGTH_LONG
            ).show()
        }

        dashboard_swipe_container.setOnRefreshListener {
            eventViewModel.loadEventsByLocation()
        }

        dashboard_fab.setOnClickListener {
            (activity as NavigationActivity).showDashnoardSearchDialogFragment()
        }
        dashboard_swipe_container.setColorSchemeResources(R.color.colorAccent)
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }
}
