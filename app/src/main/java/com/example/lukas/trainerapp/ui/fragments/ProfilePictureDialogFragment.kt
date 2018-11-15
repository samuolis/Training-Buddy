package com.example.lukas.trainerapp.ui.fragments


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.ui.adapters.ProfilePictureRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_profile_picture_dialog.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfilePictureDialogFragment : DialogFragment() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var userViewModel : UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_profile_picture_dialog, container, false)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        userViewModel.user.observe(this, Observer {
            viewManager = GridLayoutManager(activity, 2)
            viewAdapter = ProfilePictureRecyclerViewAdapter(userViewModel.bitmap, activity = activity as NavigationActivity, user = it)
            profile_picture_recycler_view.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
        })
        return rootView
    }


}
