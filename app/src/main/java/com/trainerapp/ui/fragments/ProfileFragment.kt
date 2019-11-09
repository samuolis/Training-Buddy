package com.trainerapp.ui.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.enums.EventDetailScreen
import com.trainerapp.enums.ProfilePicture
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.models.User
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.UserEventsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.utils.DrawableUtils
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject


class ProfileFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var eventViewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInfo()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventViewModel = getViewModel(viewModelFactory)
        eventViewModel.loadUserEventsByIds()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            eventViewModel.loadUserEventsByIds()
        }
    }

    private fun setupInfo() {
        profile_events_recycler_view.layoutManager = LinearLayoutManager(context) as RecyclerView.LayoutManager?
        eventViewModel.refreshStatus.observe(this, Observer {
            profile_swipe_container.isRefreshing = !(it == 0)
        })
        eventViewModel.error.nonNullObserve(this) {
            Toast.makeText(
                    activity,
                    "Failed to get data: " + it.localizedMessage,
                    Toast.LENGTH_LONG
            ).show()
        }
        profile_swipe_container.setOnRefreshListener {
            eventViewModel.loadUserEventsByIds()
        }
        profile_swipe_container.setColorSchemeResources(R.color.colorAccent)
        eventViewModel.user.observe(this, Observer { user: User ->
            initials_image_view.post {
                if (user.profilePictureIndex == null || user.profilePictureIndex!! >= ProfilePicture.values().size) {
                    DrawableUtils.setupInitials(initials_image_view, user)
                } else {
                    initials_image_view.setImageResource(ProfilePicture.values()[user.profilePictureIndex!!].drawableId)
                }
            }
            user_full_name_text_view.text = user.fullName
            profile_linear_layout.visibility = View.VISIBLE
        })
        user_full_name_text_view.setOnClickListener {
            (activity as NavigationActivity).showAccountEditDialogFragment()
        }
        initials_image_view.setOnClickListener {
            (activity as NavigationActivity).showAccountEditDialogFragment()
        }

        eventViewModel.userEvents.nonNullObserve(this) { userEvents ->

            profile_events_recycler_view.adapter = UserEventsRecyclerViewAdapter(
                    userEvents,
                    context!!
            ) { position ->
                (activity as NavigationActivity)
                        .showEventDetailsDialogFragment(
                                userEvents[position].eventId!!,
                                EventDetailScreen.PROFILE
                        )
            }
        }
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }
}
