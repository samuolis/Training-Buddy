package com.example.lukas.trainerapp.ui.fragments


import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_event_details_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.ui.adapters.EventDetailsRecyclerViewAdapter
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel
import com.example.lukas.trainerapp.web.webservice.EventWebService
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class EventDetailsDialogFragment : DialogFragment() {

    lateinit var eventViewModel: EventViewModel
    lateinit var eventWebService: EventWebService
    lateinit var userViewModel: UserViewModel
    lateinit var userId: String
    var eventId: Long? = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_event_details_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
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

            eventViewModel.getStatusForDescription()?.observe(this, Observer {status ->
                when (status) {
                    0 -> setupDashboardUI()
                    1 -> setupProfileUI()
                    2 -> setupHomeUI()
                }

            })


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



    private fun setupHomeUI() {
        setHasOptionsMenu(true)
        eventViewModel?.getEventByPosition()?.observe(this, androidx.lifecycle.Observer {
            eventId = it.eventId
            eventViewModel.loadSignedUserList(it.eventSignedPlayers)
            event_details_title.text = SpannableStringBuilder(it.eventName)
            val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val dateStr = timeStampFormat.format(it.eventDate)
            event_details_date.text = SpannableStringBuilder(dateStr)
            event_details_description.text = SpannableStringBuilder(it.eventDescription)
            if (it.eventDistance == null) {
                event_details_distance_layout.visibility = View.GONE
                event_details_location.setPadding(0, 0, 0, 32)
            } else {
                event_details_distance.text = DecimalFormat("##.##").format(it.eventDistance)
                event_details_location.setPadding(0, 0, 0, 0)
            }
            event_details_players_spot_left.text = SpannableStringBuilder(it.eventPlayers.toString())
            event_details_location.text = SpannableStringBuilder(it.eventLocationName)
            event_details_submit_button.text = getString(R.string.edit_event_button_label)
            event_details_submit_button.setOnClickListener { view ->
                (activity as NavigationActivity).showEventCreateDialogFragment()
            }

        })
    }

    fun setupDashboardUI() {
        eventViewModel.getEventInDashboard()?.observe(this, Observer {
            eventViewModel.loadSignedUserList(it.eventSignedPlayers)
            event_details_title.text = SpannableStringBuilder(it.eventName)
            val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val dateStr = timeStampFormat.format(it.eventDate)
            event_details_date.text = SpannableStringBuilder(dateStr)
            event_details_description.text = SpannableStringBuilder(it.eventDescription)
            if (it.eventDistance == null) {
                event_details_distance_layout.visibility = View.GONE
                event_details_location.setPadding(0, 0, 0, 16)
            } else {
                event_details_distance.text = DecimalFormat("##.##").format(it.eventDistance)
                event_details_location.setPadding(0, 0, 0, 0)
            }
            event_details_players_spot_left.text = SpannableStringBuilder(it.eventPlayers.toString())
            event_details_location.text = SpannableStringBuilder(it.eventLocationName)
            event_details_submit_button.text = getString(R.string.event_description_positive_button)
            event_details_submit_button.setOnClickListener { view ->
                eventWebService.signEvent(userId, it.eventId).enqueue(object : Callback<Void> {
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
        })
    }

    fun setupProfileUI() {
        eventViewModel.getEventInProfile()?.observe(this, Observer {
            eventViewModel.loadSignedUserList(it.eventSignedPlayers)
            event_details_title.text = SpannableStringBuilder(it.eventName)
            val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val dateStr = timeStampFormat.format(it.eventDate)
            event_details_date.text = SpannableStringBuilder(dateStr)
            event_details_description.text = SpannableStringBuilder(it.eventDescription)
            if (it.eventDistance == null){
                event_details_distance_layout.visibility = View.GONE
                event_details_location.setPadding(0,0,0,32)
            } else{
                event_details_distance.text = DecimalFormat("##.##").format(it.eventDistance)
                event_details_location.setPadding(0,0,0,0)
            }
            event_details_players_spot_left.text = SpannableStringBuilder(it.eventPlayers.toString())
            event_details_location.text = SpannableStringBuilder(it.eventLocationName)
            event_details_submit_button.text = getString(R.string.event_description_negative_button)
            event_details_submit_button.setOnClickListener {view ->
                eventWebService.unsignEvent(userId, it.eventId).enqueue(object : Callback<Void>{
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                .show()
                    }
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        (activity as NavigationActivity).backOnStack()
                        eventViewModel?.loadUserData()
                        eventViewModel?.loadEventsByLocation()
                    }

                })
            }
        })
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
            eventWebService.deleteEventById(eventId).enqueue(object : Callback<Void>{
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "failed with " + t.message, Toast.LENGTH_LONG)
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    (activity as NavigationActivity).getBackOnStackToMainMenu()
                    eventViewModel?.loadEvents()
                }

            })
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
