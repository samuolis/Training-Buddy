package com.example.lukas.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.lukas.trainerapp.R

class EventDetailsDialogFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_event_details_dialog, container, false)
        return rootView
    }


}
