package com.example.lukas.trainerapp.ui.fragments


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_home, container, false)
        rootView.post {
            fab.setOnClickListener { view ->
                (activity as NavigationActivity).showEventCreateDialogFragment()
            }
        }
        return rootView
    }


}
