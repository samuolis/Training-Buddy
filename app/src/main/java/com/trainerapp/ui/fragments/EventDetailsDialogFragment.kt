package com.trainerapp.ui.fragments


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
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
import com.google.gson.GsonBuilder
import com.trainerapp.db.entity.Event
import com.trainerapp.models.CommentMessage
import com.trainerapp.ui.adapters.EventDetailsCommentsRecyclerViewAdapter
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
            event_details_recycler_view.layoutManager = LinearLayoutManager(context)
            event_comments_recycler_view.layoutManager = LinearLayoutManager(context)

            comment_edit_text.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                   var editTextString = comment_edit_text.text.toString().trim()
                    confirm_message_submit_text_view.isEnabled = !editTextString.isEmpty()
                }

            })

            eventViewModel.changeLoadStatus(1)

            eventViewModel.getLoadingStatus()?.observe(this, Observer {
                when (it) {
                    0 -> hideProgressBar()
                    1 -> showProgressBar()
                }
            })

            eventViewModel.getSignedUsers()?.observe(this, Observer {
                event_details_recycler_view.adapter = EventDetailsRecyclerViewAdapter(it ,context!!)
            })

            eventViewModel.getEventComments()?.observe(this, Observer {
                event_comments_recycler_view.adapter = EventDetailsCommentsRecyclerViewAdapter(it, context!!)
            })

            setupUI()

        }
//        val actionBar = (activity as AppCompatActivity).supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeButtonEnabled(true)
//            actionBar.title = getString(R.string.event_details_title_label)
//            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
//        }
        return rootView
    }

    fun sendCommentMessage(eventId: Long?){
        var message = comment_edit_text.text.toString()
        var timeNow = Calendar.getInstance().timeInMillis
        var commentMessage = CommentMessage(message, userId, eventId, timeNow, "")
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful()) {
                var token = it.getResult()?.getToken();
                eventWebService.createCommentMessage(commentMessage, token).enqueue(object : Callback<CommentMessage>{
                    override fun onFailure(call: Call<CommentMessage>, t: Throwable) {
                        Snackbar.make(event_details_layout, "Failed to send message", Snackbar.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<CommentMessage>, response: Response<CommentMessage>) {
                        comment_edit_text.text = SpannableStringBuilder("")
                        eventViewModel.loadEventComments(true)
                    }

                })
            }
        }

    }

    private fun setupUI(){
        eventViewModel?.getDetailsOneEvent()?.observe(this, androidx.lifecycle.Observer {
            eventViewModel.getStatusForDescription()?.observe(this, Observer { status ->
                when (status) {
                    0 -> setupDashboardUI(it)
                    1 -> setupProfileUI(it)
                    2 -> setupHomeUI(it)
                }

            })
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
        event_details_submit_button.setOnClickListener { view ->
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    var token = task.getResult()?.getToken()
                    eventWebService.signEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            eventViewModel?.loadEventsByLocation()
                            eventViewModel.loadUserData()
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
            currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    var token = task.getResult()?.getToken()
                    eventWebService.unsignEvent(userId, event.eventId, token).enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            eventViewModel?.loadUserData()
                            eventViewModel?.loadOneEventInUserProfile()
                        }

                    })
                }
            }
        }
    }

    private fun updateUI(event: Event){
        eventViewModel.loadSignedUserList(event.eventSignedPlayers)
        eventViewModel.loadEventComments()
        event_details_title.text = SpannableStringBuilder(event.eventName)
        val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        timeStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        val dateStr = timeStampFormat.format(event.eventDate)
        event_details_date.text = SpannableStringBuilder(dateStr)
        event_details_description.text = SpannableStringBuilder(event.eventDescription)
        if (event.eventDistance == null){
            event_details_distance_layout.visibility = View.GONE
            event_details_location.setPadding(0,0,0,32)
        } else{
            event_details_distance.text = DecimalFormat("##.##").format(event.eventDistance)
            event_details_location.setPadding(0,0,0,0)
        }
        event_details_players_spot_left.text = SpannableStringBuilder(event.eventPlayers.toString())
        event_details_location.text = SpannableStringBuilder(event.eventLocationName)
        event_details_submit_button.text = getString(R.string.event_description_negative_button)
        confirm_message_submit_text_view.setOnClickListener { view ->
            if (comment_edit_text.text.toString().length <= 100){
                sendCommentMessage(event.eventId)
            } else{
                Snackbar.make(event_details_layout, "Text is too long", Snackbar.LENGTH_SHORT).show()
            }
        }
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


}
