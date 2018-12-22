package com.trainerapp.ui.fragments


import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import com.trainerapp.R
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.ui.NavigationActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_search, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        val userSharedPref = activity?.getSharedPreferences(context
                ?.getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        val editor = userSharedPref?.edit()
        rootView.post {
            var radiusValue = userSharedPref?.getString(context?.getString(R.string.user_prefered_distance_key), "30")
            events_radius_edit_text.text = SpannableStringBuilder(radiusValue)
            search_fab.setOnClickListener {
                if (events_radius_edit_text.text != null) {
                    editor?.putString(getString(R.string.user_prefered_distance_key), events_radius_edit_text.text.toString())
                    editor?.commit()
                    eventViewModel?.loadEventsByLocation()
                    (activity as NavigationActivity).backOnStack()
                } else{
                    Snackbar.make(it, "Some fields are missing", Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        }
        return rootView
    }


}
