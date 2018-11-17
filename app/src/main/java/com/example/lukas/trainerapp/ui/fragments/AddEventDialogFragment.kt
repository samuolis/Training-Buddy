package com.example.lukas.trainerapp.ui.fragments


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import com.example.lukas.trainerapp.R
import kotlinx.android.synthetic.main.fragment_add_event_dialog.*
import java.util.*
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import android.content.Intent
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.db.entity.Event
import com.example.lukas.trainerapp.db.viewmodel.EventViewModel
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.webService.EventWebService
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AddEventDialogFragment : DialogFragment() {

    val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    lateinit var userViewModel: UserViewModel
    var date_time = ""
    var mYear: Int = 0
    var mMonth: Int = 0
    var mDay: Int = 0
    var selectedLocationName: String? =  null
    var selectedLocationLatitude: Double? = null
    var selectedLocationLongitude: Double? = null
    var dateAndTimeIsSet: Boolean = false
    var eventViewModel: EventViewModel? = null;

    var mHour: Int = 0
    var mMinute: Int = 0
    val c = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_add_event_dialog, container, false)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)

        rootView.post {
            event_date_time_text_view.setOnClickListener {
                datePicker()
            }
            event_location_edit_text.onFocusChangeListener = View.OnFocusChangeListener({ view: View, b: Boolean ->
                if (b == true) {
                    try {
                        val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(activity)
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                    } catch (e: GooglePlayServicesRepairableException) {
                        // TODO: Handle the error.
                    } catch (e: GooglePlayServicesNotAvailableException) {
                        // TODO: Handle the error.
                    }
                }
            })

            event_fab.setOnClickListener { view ->
                saveEvent(view)
            }
        }
        return rootView
    }

    private fun saveEvent(view: View) {
        var selectedDateAndTime = c.time
        if(selectedLocationName == null || event_name_edit_text.text == null || !dateAndTimeIsSet){
            Snackbar.make(view, "Some fields are missing", Snackbar.LENGTH_LONG)
                    .show()
            return
        }


        var eventPlayersNumber: Int? = null
        if (event_players_edit_text.text.toString() == ""){
            eventPlayersNumber = 0
        } else{
            eventPlayersNumber = event_players_edit_text.text?.toString()?.toInt()
        }
        val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(userViewModel.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        val eventWebService = retrofit.create(EventWebService::class.java)
        var event : Event
        userViewModel.user.observe(this, androidx.lifecycle.Observer {
             event = Event(null, it.userId, event_name_edit_text.text?.toString(), event_description_edit_text.text?.toString(),
                     selectedLocationName, selectedLocationLatitude,
                     selectedLocationLongitude, eventDate = selectedDateAndTime,
                     eventPlayers = eventPlayersNumber)

            eventWebService.createEvent(event).enqueue(object : Callback<Event> {
                override fun onFailure(call: Call<Event>, t: Throwable) {
                    Toast.makeText(context, "failed with " + t.message, Toast.LENGTH_LONG)
                }

                override fun onResponse(call: Call<Event>, response: Response<Event>) {
                    (activity as NavigationActivity).backOnStack()
                    Snackbar.make(view, "Event added", Snackbar.LENGTH_LONG)
                            .setAction("Remove", {

                            })
                            .show()
                    eventViewModel?.loadEvents()
                }

            })
    })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(context, data)
                selectedLocationLatitude = place.latLng.latitude
                selectedLocationLongitude = place.latLng.longitude
                selectedLocationName = place.name.toString()
                event_location_edit_text.text = SpannableStringBuilder(place.address)
                Log.i(TAG, "Place: " + place.name)
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(context, data)
                // TODO: Handle the error.
                Log.i(TAG, status.statusMessage)

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private fun datePicker() {

        // Get Current Date
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    date_time = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                    c.set(year, monthOfYear, dayOfMonth)
                    //*************Call Time Picker Here ********************
                    timePicker()
                }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private fun timePicker() {
        // Get Current Time

        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    var time = String.format("%02d:%02d", hourOfDay, minute)
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    c.set(Calendar.MINUTE, minute)
                    dateAndTimeIsSet = true
                    event_date_time_text_view.setText("$date_time $time")
                }, mHour, mMinute, true)
        timePickerDialog.show()
    }


}
