package com.trainerapp.ui.fragments


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.trainerapp.R
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_event_details_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.adapters.EventDetailsRecyclerViewAdapter
import com.trainerapp.web.webservice.EventWebService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.trainerapp.db.entity.Event
import com.trainerapp.models.CommentMessage
import com.trainerapp.ui.adapters.CommentsDetailsRecyclerViewAdapter
import com.trainerapp.ui.adapters.EventCommentsRecyclerViewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class EventDetailsDialogFragment : DialogFragment() {

    lateinit var eventViewModel: EventViewModel
    lateinit var eventWebService: EventWebService
    lateinit var userId: String
    lateinit var auth: FirebaseAuth
    var eventId: Long? = 0
    var currentUser: FirebaseUser? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_event_details_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        var userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0") ?: "0"
        val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(eventViewModel?.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        eventWebService = retrofit.create(EventWebService::class.java)

        rootView.post {
            event_details_recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            event_comments_recycler_view.layoutManager = LinearLayoutManager(context)

            signed_users_layout.setOnClickListener {
                (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
            }
            event_details_recycler_view.setOnClickListener {
                (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
            }

            event_comments_label.setOnClickListener {
                (activity as NavigationActivity).showEventCommentsDialogFragment()
            }

            eventViewModel.changeLoadStatus(1)

            eventViewModel.getLoadingStatus()?.observe(this, Observer {
                when (it) {
                    0 -> hideProgressBar()
                    1 -> showProgressBar()
                }
            })

            eventViewModel.getSignedUsers()?.observe(this, Observer {
                event_details_recycler_view.adapter = EventDetailsRecyclerViewAdapter(it ,context!!,
                        object : EventDetailsRecyclerViewAdapter.MyClickListener{
                    override fun onItemClicked(position: Int) {
                        (activity as NavigationActivity).showEventSignedUsersListDialogFragment()
                    }

                })
            })

            eventViewModel.getEventComments()?.observe(this, Observer {
                var commentsList = mutableListOf<CommentMessage>()
                if (it.size > 2){
                    commentsList.add(CommentMessage("View " + (it.size - 2) + " more comments...", null, null,
                            null, ""))
                    commentsList.add(it[it.size - 2])
                    commentsList.add(it[it.size - 1])
                } else{
                    commentsList = it.toMutableList()
                }
                event_comments_recycler_view.adapter = EventCommentsRecyclerViewAdapter(commentsList, context!!,
                        object : EventCommentsRecyclerViewAdapter.MyClickListener{
                            override fun onItemClicked(position: Int) {
                                (activity as NavigationActivity).showEventCommentsDialogFragment()
                            }

                        })
            })

            setupUI()

        }
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.event_details_title_label)
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }
        return rootView
    }

    private fun setupUI(){
        eventViewModel?.getDetailsOneEvent()?.observe(this, androidx.lifecycle.Observer {
            if (userId == it.userId) {
                setupHomeUI(it)
            } else if (it.eventSignedPlayers != null && it.eventSignedPlayers.contains(userId)){
                setupProfileUI(it)
            } else{
                setupDashboardUI(it)
            }
//            when (status) {
//                0 -> setupDashboardUI(it)
//                1 -> setupProfileUI(it)
//                2 -> setupHomeUI(it)
//
//            })
        })
    }



    private fun setupHomeUI(event: Event) {
        setHasOptionsMenu(true)
        eventId = event.eventId
        updateUI(event)
        event_details_submit_button.text = getString(R.string.edit_event_button_label)
        event_details_submit_button.setOnClickListener { view ->
            (activity as NavigationActivity).showEventCreateDialogFragment()
        }

    }

    fun setupDashboardUI(event: Event) {
        eventId = event.eventId
        updateUI(event)
        event_details_submit_button.text = getString(R.string.event_description_positive_button)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    var token = task.getResult()?.getToken()
                    eventWebService.signEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                            hideProgressBar()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            (activity as NavigationActivity).cleanCashedData()
                            eventViewModel?.loadEventsByLocation()
                        }

                    })
                }
            }
        }

    }

    fun setupProfileUI(event: Event) {
        eventId = event.eventId
        updateUI(event)
        event_details_submit_button.setOnClickListener { view ->
            event_details_submit_button.isEnabled = false
            showProgressBar()
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    var token = task.getResult()?.getToken()
                    eventWebService.unsignEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                            hideProgressBar()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            (activity as NavigationActivity).cleanCashedData()
                            eventViewModel?.loadUserData()
                        }

                    })
                }
            }
        }
    }

    private fun updateUI(event: Event){
        event_location_layout.setOnClickListener {
            var gmmIntentUri = Uri.parse("geo:" + event.eventLocationLatitude +"," +
                    event.eventLocationLongitude + "?q=" + event.eventLocationLatitude +"," +
                    event.eventLocationLongitude + "(" + event.eventName + ")")
            var mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
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
        event_details_layout.visibility = View.VISIBLE
    }

    private fun showProgressBar() {
        progress_bar_background_event_details.setVisibility(View.VISIBLE)
        login_progress_event_details.setVisibility(View.VISIBLE)
    }

    private fun hideProgressBar() {
        progress_bar_background_event_details.setVisibility(View.GONE)
        login_progress_event_details.setVisibility(View.GONE)
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
                    var token = it.getResult()?.getToken();
                    eventWebService.deleteEventById(eventId, token).enqueue(object : Callback<Void>{
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "failed with " + t.message, Toast.LENGTH_LONG)
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).getBackOnStackToMainMenu()
                            eventViewModel?.loadEvents()
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

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
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
}
