package com.trainerapp.ui.fragments


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.models.CommentMessage
import com.trainerapp.models.Event
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.EventCommentsRecyclerViewAdapter
import com.trainerapp.ui.adapters.EventDetailsRecyclerViewAdapter
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_event_details_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class EventDetailsDialogFragment : BaseDialogFragment() {

    lateinit var eventViewModel: EventViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var eventWebService: EventWebService
    lateinit var userId: String
    lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private val eventId: Long by lazy {
        arguments!!.getLong(ARG_EVENT_ID)
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
        eventViewModel.loadDetailsEvent(eventId)
        event_details_recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        event_comments_recycler_view.layoutManager = LinearLayoutManager(context)

        signed_users_layout.setOnClickListener {
            (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
        }
        event_details_recycler_view.setOnClickListener {
            (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
        }

        event_comments_label.setOnClickListener {
            (activity as NavigationActivity).showEventCommentsDialogFragment(eventId)
        }

        eventViewModel.changeLoadStatus(1)

        eventViewModel.getLoadingStatus()?.nonNullObserve(this) {
            when (it) {
                0 -> hideProgressBar()
                1 -> showProgressBar()
            }
        }

        eventViewModel.getSignedUsers()?.nonNullObserve(this) {
            event_details_recycler_view.adapter = EventDetailsRecyclerViewAdapter(it, context!!,
                    object : EventDetailsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
                        }

                    })
        }

        eventViewModel.getEventComments()?.nonNullObserve(this) {
            var commentsList = mutableListOf<CommentMessage>()
            if (it.size > 2) {
                commentsList.add(CommentMessage("View " + (it.size - 2) + " more comments...", null, null,
                        null, ""))
                commentsList.add(it[it.size - 2])
                commentsList.add(it[it.size - 1])
            } else {
                commentsList = it.toMutableList()
                commentsList.add(CommentMessage("Write Message", null, null,
                        null, ""))
            }
            event_comments_recycler_view.adapter = EventCommentsRecyclerViewAdapter(commentsList, context!!,
                    object : EventCommentsRecyclerViewAdapter.MyClickListener {
                        override fun onItemClicked(position: Int) {
                            (activity as NavigationActivity).showEventCommentsDialogFragment(eventId)
                        }

                    })
        }

        setupUI()

    }

    private fun setupUI(){
        eventViewModel.getDetailsOneEvent()?.nonNullObserve(this) {
            if (userId == it.userId) {
                setupHomeUI(it)
            } else if (it.eventSignedPlayers != null && it.eventSignedPlayers.contains(userId)) {
                setupProfileUI(it)
            } else {
                setupDashboardUI(it)
            }
        }
    }



    private fun setupHomeUI(event: Event) {
        setHasOptionsMenu(true)
        updateUI(event)
        event_details_submit_button.text = getString(R.string.edit_event_button_label)
        event_details_submit_button.setOnClickListener { view ->
            (activity as NavigationActivity).showEventCreateDialogFragment()
        }

    }

    private fun setupDashboardUI(event: Event) {
        updateUI(event)
        event_details_submit_button.text = getString(R.string.event_description_positive_button)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.getResult()?.getToken()
                    eventWebService.signEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                            hideProgressBar()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            (activity as NavigationActivity).cleanCashedData()
                            eventViewModel.loadEventsByLocation()
                            FirebaseMessaging.getInstance().subscribeToTopic(event.eventId.toString())
                        }

                    })
                }
            }
        }

    }

    private fun setupProfileUI(event: Event) {
        updateUI(event)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    val token = task.getResult()?.getToken()
                    eventWebService.unsignEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                            hideProgressBar()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            (activity as NavigationActivity).cleanCashedData()
                            eventViewModel.loadUserData()
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(event.eventId.toString())
                        }

                    })
                }
            }
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
        timeStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
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
        var spotsLeft: Int? = 0
        if (event.eventSignedPlayers != null) {
            spotsLeft = event.eventPlayers!! - event.eventSignedPlayers.size
        } else{
            spotsLeft = event.eventPlayers
        }
        event_details_players_spot_left.text = SpannableStringBuilder(spotsLeft.toString())
        event_details_location.text = SpannableStringBuilder(event.eventLocationName)
        event_details_submit_button.text = getString(R.string.event_description_negative_button)
    }

    private fun showProgressBar() {
        progress_bar_background_event_details.setVisibility(View.VISIBLE)
        login_progress_event_details.setVisibility(View.VISIBLE)
    }

    private fun hideProgressBar() {
        progress_bar_background_event_details.setVisibility(View.GONE)
        login_progress_event_details.setVisibility(View.GONE)
        nestedScrollView.visibility = View.VISIBLE
        event_details_submit_button_layout.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        inflater?.inflate(R.menu.main, menu)
        menu?.add("Remove")
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
        0 -> {

            currentUser?.getIdToken(true)?.addOnCompleteListener{
                if (it.isSuccessful()) {
                    val token = it.getResult()?.getToken();
                    eventWebService.deleteEventById(eventId, token).enqueue(object : Callback<Void>{
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "failed with " + t.message, Toast.LENGTH_LONG)
                                    .show()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).getBackOnStackToMainMenu()
                            eventViewModel.loadEvents()
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(eventId.toString())
                        }

                    })
                }
            }
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
                    if (id != "" && id.toLong() == eventId) {
                        eventViewModel.loadEventComments(id.toLong())
                        eventViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                    eventViewModel.loadUserEventsByIds()
                }
                NavigationActivity.NOTIFICATION_EVENT_REFRESH_VALUE -> {
                    val id = intent.getStringExtra(NavigationActivity.EVENT_ID_INTENT)
                    if (id != "" && id.toLong() == eventId) {
                        eventViewModel.loadEventComments(id.toLong())
                        eventViewModel.loadDetailsEvent(eventId = id.toLong())
                    }
                }
            }
        }
    }

    companion object {

        val ARG_EVENT_ID = "EVENT_ID"

        fun newInstance(
                eventId: Long? = null
        ): EventDetailsDialogFragment {
            return EventDetailsDialogFragment().apply {
                arguments = Bundle().apply {
                    eventId?.let {
                        putLong(ARG_EVENT_ID, it)
                    }
                }
            }
        }
    }
}
