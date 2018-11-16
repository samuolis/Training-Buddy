package com.example.lukas.trainerapp.ui.fragments


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

import com.example.lukas.trainerapp.R
import kotlinx.android.synthetic.main.fragment_add_event_dialog.*
import java.util.*
import android.widget.TimePicker
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import androidx.core.app.ShareCompat.IntentBuilder
import android.content.Intent
import android.text.SpannableStringBuilder
import android.util.Log
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import com.google.android.gms.location.places.Place
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*


class AddEventDialogFragment : DialogFragment() {

    val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1

    var date_time = ""
    var mYear: Int = 0
    var mMonth: Int = 0
    var mDay: Int = 0

    var mHour: Int = 0
    var mMinute: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_add_event_dialog, container, false)
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
                Snackbar.make(view, "Event added", Snackbar.LENGTH_LONG)
                        .setAction("Remove", {

                        })
                        .show()
                (activity as NavigationActivity).backOnStack()
            }
        }
        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(context, data)
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
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    date_time = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                    //*************Call Time Picker Here ********************
                    timePicker()
                }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private fun timePicker() {
        // Get Current Time
        val c = Calendar.getInstance()
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    var time = String.format("%02d:%02d", hourOfDay, minute)

                    event_date_time_text_view.setText("$date_time $time")
                }, mHour, mMinute, true)
        timePickerDialog.show()
    }


}
