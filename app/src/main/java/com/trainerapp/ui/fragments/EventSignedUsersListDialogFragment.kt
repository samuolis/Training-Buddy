package com.trainerapp.ui.fragments


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.trainerapp.R
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.EventSignedUsersRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_event_signed_users_list_dialog.*

/**
 * A simple [Fragment] subclass.
 *
 */
class EventSignedUsersListDialogFragment : DialogFragment() {

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_event_signed_users_list_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)

        rootView.post {
            event_signed_players_list_recycler_view.layoutManager = LinearLayoutManager(context)
            eventViewModel.getSignedUsers()?.observe(this, Observer {
                event_signed_players_list_recycler_view.adapter = EventSignedUsersRecyclerViewAdapter(it ,context!!, null)
            })
        }

        return rootView
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
}
