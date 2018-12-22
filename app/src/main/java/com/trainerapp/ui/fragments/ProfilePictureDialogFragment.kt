package com.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.trainerapp.R
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.ProfilePictureRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_profile_picture_dialog.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfilePictureDialogFragment : DialogFragment() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_profile_picture_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        rootView.post {
            viewManager = GridLayoutManager(activity, 2)
            viewAdapter = ProfilePictureRecyclerViewAdapter(activity = activity as NavigationActivity,
                    onClickListener = object : ProfilePictureRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            eventViewModel.loadProfilePicture(position)
                            (activity as NavigationActivity).backOnStack()
                        }

                    })
            profile_picture_recycler_view.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
        }

        return rootView
    }


}
