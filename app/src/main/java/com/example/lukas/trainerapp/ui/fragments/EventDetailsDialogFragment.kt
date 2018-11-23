package com.example.lukas.trainerapp.ui.fragments


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.fragment_event_details_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.webService.EventWebService
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_event_details_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        var userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        var userId = userSharedPref?.getString(getString(R.string.user_id_key), "0")
        val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(eventViewModel?.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        val eventWebService = retrofit.create(EventWebService::class.java)

        rootView.post {
            eventViewModel.oneEventDashboard?.observe(this, Observer {
                event_details_title.text = SpannableStringBuilder(it.eventName)
                val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                val dateStr = timeStampFormat.format(it.eventDate)
                event_details_date.text = SpannableStringBuilder(dateStr)
                event_details_description.text = SpannableStringBuilder(it.eventDescription)
                event_details_distance.text = DecimalFormat("##.##").format(it.eventDistance)
                event_details_players_spot_left.text = SpannableStringBuilder(it.eventPlayers.toString())
                event_details_location.text = SpannableStringBuilder(it.eventLocationName)
                event_details_submit_button.setOnClickListener {view ->
                    eventWebService.signEvent(userId, it.eventId).enqueue(object : Callback<Void>{
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Snackbar.make(view, "Error " + t.message, Snackbar.LENGTH_LONG)
                                    .show()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            (activity as NavigationActivity).backOnStack()
                            Snackbar.make(view, "Event signed", Snackbar.LENGTH_LONG)
                                    .setAction("Unsign", {

                                    })
                                    .show()
                            eventViewModel?.loadEventsByLocation()
                        }

                    })
                }

            })
        }
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = ""
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        }
        return rootView
    }
}
