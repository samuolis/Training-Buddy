package com.trainerapp.ui.fragments


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import com.trainerapp.R
import kotlinx.android.synthetic.main.fragment_add_event_dialog.*
import java.util.*
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import android.content.Intent
import android.location.Geocoder
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.trainerapp.db.entity.Event
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.web.webservice.EventWebService
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat


class AddEventDialogFragment : DialogFragment() {

    val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var date_time = ""
    var mYear: Int = 0
    var mMonth: Int = 0
    var mDay: Int = 0
    var selectedLocationName: String? =  null
    var selectedLocationLatitude: Double? = null
    var selectedLocationLongitude: Double? = null
    var selectedLocationCountryCode: String? = null
    var dateAndTimeIsSet: Boolean = false
    var eventId: Long? = null
    var userId: String? = null
    var eventViewModel: EventViewModel? = null
    var selectedDateAndTime: Date? = null
    var eventSignedPlayers: List<String>? = null

    var mHour: Int = 0
    var mMinute: Int = 0
    val c = Calendar.getInstance()
    lateinit var auth: FirebaseAuth


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_add_event_dialog, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        var userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0")
        auth = FirebaseAuth.getInstance()

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

            eventViewModel?.getEventByPosition()?.observe(this, androidx.lifecycle.Observer {
                if (it != null) {
                    event_name_edit_text.text = SpannableStringBuilder(it.eventName)
                    event_description_edit_text.text = SpannableStringBuilder(it.eventDescription)
                    event_location_edit_text.text = SpannableStringBuilder(it.eventLocationName)
                    selectedLocationCountryCode = it.eventLocationCountryCode
                    selectedLocationLongitude = it.eventLocationLongitude
                    selectedLocationLatitude = it.eventLocationLatitude
                    selectedLocationName = it.eventLocationName
                    event_players_edit_text.text = SpannableStringBuilder(it.eventPlayers.toString())
                    eventId = it.eventId
                    userId = it.userId
                    val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                    event_date_time_text_view.text = timeStampFormat.format(it.eventDate)
                    selectedDateAndTime = it.eventDate
                    eventSignedPlayers = it.eventSignedPlayers
                }
            })

        }
        return rootView
    }

    private fun saveEvent(view: View) {
        if(selectedLocationName == null || event_name_edit_text.text == null || selectedDateAndTime == null){
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
                .baseUrl(eventViewModel?.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        val eventWebService = retrofit.create(EventWebService::class.java)
        var event : Event

        event = Event(eventId, userId, event_name_edit_text.text?.toString(), event_description_edit_text.text?.toString(),
                selectedLocationName, selectedLocationLatitude,
                selectedLocationLongitude, selectedLocationCountryCode, eventDate = selectedDateAndTime,
                eventPlayers = eventPlayersNumber, eventDistance = null, eventSignedPlayers = eventSignedPlayers)

        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful()) {
                var token = it.getResult()?.getToken();
                eventWebService.createEvent(event, token).enqueue(object : Callback<Event> {
                    override fun onFailure(call: Call<Event>, t: Throwable) {
                        Toast.makeText(context, "failed with " + t.message, Toast.LENGTH_LONG)
                    }

                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        (activity as NavigationActivity).backOnStack()
                        eventViewModel?.loadEvents()
                    }

                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(context, data)
                selectedLocationLatitude = place.latLng.latitude
                selectedLocationLongitude = place.latLng.longitude
                selectedLocationName = place.name.toString()

                var geocoder = Geocoder(context, Locale.getDefault())
                var adresses = geocoder.getFromLocation(place.latLng.latitude,
                        place.latLng.longitude, 1)
                var address = adresses[0]
                selectedLocationCountryCode = address.getCountryCode()
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
                    selectedDateAndTime = c.time
                    val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                    event_date_time_text_view.text = timeStampFormat.format(selectedDateAndTime)
                }, mHour, mMinute, true)
        timePickerDialog.show()
    }


}
