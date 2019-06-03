package com.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.ui.adapters.EventSignedUsersRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import kotlinx.android.synthetic.main.fragment_event_signed_users_list_dialog.*
import javax.inject.Inject

class EventSignedUsersListDialogFragment : BaseDialogFragment() {

    lateinit var eventDetailsViewModel: EventDetailsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_signed_users_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventDetailsViewModel = getViewModel(viewModelFactory)
        event_signed_players_list_recycler_view.layoutManager = LinearLayoutManager(context)
        eventDetailsViewModel.detailsOneEvent.observe(this, Observer { event ->
            event_signed_players_list_recycler_view.adapter = EventSignedUsersRecyclerViewAdapter(
                    usersList = event.eventSignedPlayers,
                    context = context!!,
                    onClickListener = null
            )
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.event_details_title_label)
        }
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }
}
