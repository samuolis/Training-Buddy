package com.trainerapp.ui.fragments


import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.trainerapp.R
import com.trainerapp.base.BaseDialogFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.models.CommentMessage
import com.trainerapp.models.Event
import com.trainerapp.models.User
import com.trainerapp.ui.customui.InputFilterMinMax
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_add_event_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class AddEventDialogFragment : BaseDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var date_time = ""
    var mYear: Int = 0
    var mMonth: Int = 0
    var mDay: Int = 0
    var selectedLocationName: String? = null
    var selectedLocationLatitude: Double? = null
    var selectedLocationLongitude: Double? = null
    var selectedLocationCountryCode: String? = null
    var userId: String? = null
    lateinit var eventViewModel: EventViewModel
    var selectedDateAndTime: Date? = null
    var eventSignedPlayers: List<User>? = null
    var eventCommentMessage: List<CommentMessage>? = null

    var mHour: Int = 0
    var mMinute: Int = 0
    val c = Calendar.getInstance()
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    @Inject
    lateinit var eventWebService: EventWebService

    lateinit var eventDetailsViewModel: EventDetailsViewModel

    private val eventId: Long by lazy {
        arguments!!.getLong(EventDetailsDialogFragment.ARG_EVENT_ID, -1)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_add_event_dialog, container, false)

        val userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(getString(R.string.user_id_key), "0")

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventViewModel = getViewModel(viewModelFactory)
        eventDetailsViewModel = getViewModel(viewModelFactory)
        event_date_time_text_view.setOnClickListener {
            datePicker()
        }

        event_players_edit_text.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "1000"))
        event_name_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
        event_description_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(500))

        event_location_edit_text.onFocusChangeListener = View.OnFocusChangeListener { view: View, b: Boolean ->
            if (b) {
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
        }

        event_fab.setOnClickListener { view ->
            saveEvent(view)
        }

        if (eventId > -1) {
            eventDetailsViewModel.loadDetailsEvent(eventId)
            setupEditEvent()
        }
    }

    private fun setupEditEvent() {
        eventDetailsViewModel.detailsOneEvent.observe(this, androidx.lifecycle.Observer {
            event_name_edit_text.text = SpannableStringBuilder(it.eventName)
            event_description_edit_text.text = SpannableStringBuilder(it.eventDescription)
            event_location_edit_text.text = SpannableStringBuilder(it.eventLocationName)
            selectedLocationCountryCode = it.eventLocationCountryCode
            selectedLocationLongitude = it.eventLocationLongitude
            selectedLocationLatitude = it.eventLocationLatitude
            selectedLocationName = it.eventLocationName
            event_players_edit_text.text = SpannableStringBuilder(it.eventPlayers.toString())
            userId = it.userId
            val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            timeStampFormat.timeZone = TimeZone.getTimeZone("UTC")
            event_date_time_text_view.text = timeStampFormat.format(it.eventDate)
            selectedDateAndTime = it.eventDate
            eventSignedPlayers = it.eventSignedPlayers
            eventCommentMessage = it.eventComments
        })
    }

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    private fun saveEvent(view: View) {
        if (selectedLocationName == null || event_name_edit_text.text == null || selectedDateAndTime == null) {
            Snackbar.make(view, "Some fields are missing", Snackbar.LENGTH_LONG)
                    .show()
            return
        }
        if (event_players_edit_text.text.toString().toInt() < 1) {
            Snackbar.make(view, "Some fields are incorrect", Snackbar.LENGTH_LONG)
                    .show()
            return
        }

        val createEventId = if (eventId > -1) {
            eventId
        } else {
            null
        }

        val eventPlayersNumber: Int? = if (event_players_edit_text.text.toString() == "") {
            1
        } else {
            event_players_edit_text.text?.toString()?.toInt()
        }

        val event = Event(
                eventId = createEventId,
                userId = userId,
                eventName = event_name_edit_text.text?.toString(),
                eventDescription = event_description_edit_text.text?.toString(),
                eventLocationName = selectedLocationName,
                eventLocationLatitude = selectedLocationLatitude,
                eventLocationLongitude = selectedLocationLongitude,
                eventLocationCountryCode = selectedLocationCountryCode,
                eventDate = selectedDateAndTime,
                eventPlayers = eventPlayersNumber,
                eventDistance = null,
                eventSignedPlayers = eventSignedPlayers,
                eventComments = eventCommentMessage
        )
        eventDetailsViewModel.createEvent(event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(context, data)
                selectedLocationLatitude = place.latLng.latitude
                selectedLocationLongitude = place.latLng.longitude
                selectedLocationName = place.name.toString()

                val geocoder = Geocoder(context, Locale.getDefault())
                val adresses = geocoder.getFromLocation(place.latLng.latitude,
                        place.latLng.longitude, 1)
                val address = adresses[0]
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

        val datePickerDialog = DatePickerDialog(context!!,
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
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    c.set(Calendar.MINUTE, minute)
                    selectedDateAndTime = c.time
                    val timeStampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                    event_date_time_text_view.text = timeStampFormat.format(selectedDateAndTime)
                }, mHour, mMinute, true)
        timePickerDialog.show()
    }

    companion object {
        val ARG_EVENT_ID = "EVENT_ID"

        fun newInstance(
                eventId: Long? = null
        ): AddEventDialogFragment {
            return AddEventDialogFragment().apply {
                arguments = Bundle().apply {
                    eventId?.let {
                        putLong(ARG_EVENT_ID, it)
                    }
                }
            }
        }
    }
}
