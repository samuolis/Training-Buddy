package com.trainerapp.ui.fragments


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.enums.EventDetailScreen
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.models.CommentMessage
import com.trainerapp.models.Event
import com.trainerapp.navigation.NavigationController
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.EventCommentsRecyclerViewAdapter
import com.trainerapp.ui.adapters.EventDetailsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_event_details_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class EventDetailsDialogFragment : BaseDialogFragment() {

    lateinit var eventViewModel: EventViewModel
    lateinit var eventDetailsViewModel: EventDetailsViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var eventWebService: EventWebService
    @Inject
    lateinit var navigationController: NavigationController
    lateinit var userId: String
    lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private val eventId: Long by lazy {
        arguments!!.getLong(ARG_EVENT_ID)
    }

    private val eventScreen: String? by lazy {
        arguments!!.getString(ARG_EVENT_SCREEN)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_event_details_dialog, container, false)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        val userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0") ?: "0"

        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.event_details_title_label)
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }
        return rootView
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventViewModel = getViewModel(viewModelFactory)
        super.onViewCreated(view, savedInstanceState)
        eventDetailsViewModel = getViewModel(viewModelFactory)
        eventDetailsViewModel.loadDetailsEvent(eventId)
        eventDetailsViewModel.loadingStatus.nonNullObserve(this) { loading ->
            if (loading) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        }
        event_details_recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        event_comments_recycler_view.layoutManager = LinearLayoutManager(context)

        signed_users_layout.setOnClickListener {
            navigationController.showEventSignedUsersListDialogFragment()
        }
        event_details_recycler_view.setOnClickListener {
            navigationController.showEventSignedUsersListDialogFragment()
        }

        event_comments_label.setOnClickListener {
            navigationController.showEventCommentsDialogFragment(eventId)
        }

        eventDetailsViewModel.error.nonNullObserve(this) {
            Snackbar.make(view, "Error " + it.message, Snackbar.LENGTH_LONG)
                    .show()
            eventDetailsViewModel
        }

        setupUI()

    }

    private fun setupUI() {
        eventDetailsViewModel.detailsOneEvent.nonNullObserve(this) { event ->

            event_details_recycler_view.adapter = EventDetailsRecyclerViewAdapter(event.eventSignedPlayers, context!!,
                    object : EventDetailsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            navigationController.showEventSignedUsersListDialogFragment()
                        }

                    })

            setupCommentsAdapter(event.eventComments)

            when (eventScreen) {
                EventDetailScreen.DASHBOARD.name -> setupDashboardUI(event)
                EventDetailScreen.PROFILE.name -> setupProfileUI(event)
                EventDetailScreen.HOME.name -> setupHomeUI(event)
            }
        }
    }

    private fun setupCommentsAdapter(commentMessages: List<CommentMessage>?) {
        var commentsList = mutableListOf<CommentMessage>()

        if (commentMessages != null && commentMessages.size > 2) {
            commentsList.add(CommentMessage(
                    messageText = "View " + (commentMessages.size - 2) + " more comments...",
                    userId = null,
                    eventId = null,
                    messageTime = null,
                    messageUserName = ""
            ))
            commentsList.add(commentMessages[commentMessages.size - 2])
            commentsList.add(commentMessages[commentMessages.size - 1])
        } else {
            commentsList = commentMessages?.toMutableList() ?: mutableListOf()
            commentsList.add(CommentMessage(
                    messageText = "Write Message",
                    userId = null,
                    eventId = null,
                    messageTime = null,
                    messageUserName = ""
            ))
        }
        event_comments_recycler_view.adapter = EventCommentsRecyclerViewAdapter(commentsList, context!!,
                object : EventCommentsRecyclerViewAdapter.MyClickListener {
                    override fun onItemClicked(position: Int) {
                        navigationController.showEventCommentsDialogFragment(eventId)
                    }

                })
    }

    private fun setupHomeUI(event: Event) {
        setHasOptionsMenu(true)
        updateUI(event)
        event_details_submit_button.text = getString(R.string.edit_event_button_label)
        event_details_submit_button.setOnClickListener { view ->
            navigationController.showEventEditDialogFragment(eventId)
        }

    }

    private fun setupDashboardUI(event: Event) {
        updateUI(event)
        event_details_submit_button.text = getString(R.string.event_description_positive_button)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            eventDetailsViewModel.signEvent(userId, event.eventId)
        }

    }

    private fun setupProfileUI(event: Event) {
        updateUI(event)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            eventDetailsViewModel.unsignEvent(userId, event.eventId)
        }
    }

    private fun updateUI(event: Event){
        event_location_layout.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:" + event.eventLocationLatitude + "," +
                    event.eventLocationLongitude + "?q=" + event.eventLocationLatitude +"," +
                    event.eventLocationLongitude + "(" + event.eventName + ")")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)

        }
        event_details_title.text = SpannableStringBuilder(event.eventName)
        val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        timeStampFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateStr = timeStampFormat.format(event.eventDate)
        event_details_date.text = SpannableStringBuilder(dateStr)
        if (event.eventDescription == null || event.eventDescription == ""){
            event_details_description.text = SpannableStringBuilder("No description")
        } else {
            event_details_description.text = SpannableStringBuilder(event.eventDescription)
        }
        if (event.eventDistance == null){
            event_details_distance_layout.visibility = View.GONE
        } else{
            event_details_distance.text = DecimalFormat("##.##").format(event.eventDistance)
        }
        val spotsLeft: Int = if (event.eventSignedPlayers != null) {
            event.eventPlayers!! - event.eventSignedPlayers.size
        } else{
            event.eventPlayers ?: 0
        }
        event_details_players_spot_left.text = SpannableStringBuilder(spotsLeft.toString())
        event_details_location.text = SpannableStringBuilder(event.eventLocationName)
        event_details_submit_button.text = getString(R.string.event_description_negative_button)
    }

    private fun showProgressBar() {
        progress_bar_background_event_details.visibility = View.VISIBLE
        login_progress_event_details.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progress_bar_background_event_details.visibility = View.GONE
        login_progress_event_details.visibility = View.GONE
        nestedScrollView.visibility = View.VISIBLE
        event_details_submit_button_layout.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add("Remove")
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item?.itemId) {
        0 -> {
            eventDetailsViewModel.deleteEvent(eventId)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.event_details_title_label)
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(mMessageReceiver,
                IntentFilter(NavigationActivity.BROADCAST_REFRESH))
    }

    //Must unregister onPause()
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mMessageReceiver)
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(NavigationActivity.NOTIFICATION_EVENT_KEY)) {
                NavigationActivity.NOTIFICATION_EVENT_COMMENT_VALUE -> {
                    val id = intent.getStringExtra(NavigationActivity.EVENT_ID_INTENT)
                    if (id != "" && id?.toLong() == eventId) {
                        eventDetailsViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                }
                NavigationActivity.NOTIFICATION_EVENT_REFRESH_VALUE -> {
                    val id = intent.getStringExtra(NavigationActivity.EVENT_ID_INTENT)
                    if (id != "" && id?.toLong() == eventId) {
                        eventDetailsViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                }
            }
        }
    }

    companion object {

        const val ARG_EVENT_ID = "EVENT_ID"
        const val ARG_EVENT_SCREEN = "EVENT_SCREEN"

        fun newInstance(
                eventId: Long,
                eventScreen: EventDetailScreen
        ): EventDetailsDialogFragment {
            return EventDetailsDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EVENT_ID, eventId)
                    putString(ARG_EVENT_SCREEN, eventScreen.name)
                }
            }
        }
    }
}
