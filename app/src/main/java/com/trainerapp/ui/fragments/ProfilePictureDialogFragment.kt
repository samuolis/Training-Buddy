package com.trainerapp.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.extension.getViewModel
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.ProfilePictureRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_profile_picture_dialog.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfilePictureDialogFragment : BaseDialogFragment() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_picture_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventViewModel = getViewModel(viewModelFactory)
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


}
