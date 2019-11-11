package com.trainerapp.feature.add_event


import android.content.Context
import android.icu.util.Calendar
import android.location.Address
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.models.CommentMessage
import com.trainerapp.models.Event
import com.trainerapp.models.User
import com.trainerapp.ui.customui.InputFilterMinMax
import com.trainerapp.ui.fragments.EventDetailsDialogFragment
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import com.trainerapp.web.webservice.EventWebService
import kotlinx.android.synthetic.main.fragment_add_event_dialog.*
import javax.inject.Inject


class AddEventDialogFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var eventWebService: EventWebService

    lateinit var eventDetailsViewModel: EventDetailsViewModel

    lateinit var addEventViewModel: AddEventViewModel

    private val addEventLocationAutocompleteAdapter: AddEventLocationAutocompleteAdapter by lazy {
        AddEventLocationAutocompleteAdapter(context!!) {
            event_location_edit_text.setText(it.getAddressLine(0))
            event_location_edit_text.dismissDropDown()
            selectedAddress = it
        }
    }

    private var selectedAddress: Address? = null
    private val userId: String? by lazy {
        val userSharedPref = context!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userSharedPref?.getString(getString(R.string.user_id_key), "0")
    }
    private var eventSignedPlayers: List<User>? = null
    private var eventCommentMessage: List<CommentMessage>? = null

    private val eventId: Long by lazy {
        arguments!!.getLong(EventDetailsDialogFragment.ARG_EVENT_ID, -1)
    }


    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_event_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventDetailsViewModel = getViewModel(viewModelFactory)
        addEventViewModel = getViewModel(viewModelFactory)
        addEventViewModel.initialize()

        event_players_edit_text.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "1000"))
        event_name_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))
        event_description_edit_text.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(500))
        add_event_time_picker.setIs24HourView(true)
        add_event_date_picker.minDate = System.currentTimeMillis()

        event_location_edit_text.setAdapter(addEventLocationAutocompleteAdapter)
        event_location_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0 ?: return
                addEventViewModel.onLocationTextChanged(p0)
            }
        })

        addEventViewModel.addresses.nonNullObserve(this) {
            addEventLocationAutocompleteAdapter.setAddresses(it)
        }

        addEventViewModel.error.nonNullObserve(this) {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }

        addEventViewModel.loadingStatus.nonNullObserve(this) {
            changeLoadingStatus(it)
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
        eventDetailsViewModel.detailsOneEvent.observe(this, Observer {
            event_name_edit_text.text = SpannableStringBuilder(it.eventName)
            event_description_edit_text.text = SpannableStringBuilder(it.eventDescription)
            event_location_edit_text.text = SpannableStringBuilder(it.eventLocationName)
            event_players_edit_text.text = SpannableStringBuilder(it.eventPlayers.toString())
            val calendar = Calendar.getInstance()
            calendar.time = it.eventDate
            add_event_date_picker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            )
            add_event_time_picker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            add_event_time_picker.minute = calendar.get(Calendar.MINUTE)
            eventSignedPlayers = it.eventSignedPlayers
            eventCommentMessage = it.eventComments
        })
    }

    private fun saveEvent(view: View) {
        val address = selectedAddress
        if (address == null || event_name_edit_text.text == null) {
            Snackbar.make(view, "Some fields are missing", Snackbar.LENGTH_SHORT)
                    .show()
            return
        }
        if (event_players_edit_text.text.toString().toInt() < 1) {
            Snackbar.make(view, "Some fields are incorrect", Snackbar.LENGTH_SHORT)
                    .show()
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(
                add_event_date_picker.year,
                add_event_date_picker.month,
                add_event_date_picker.dayOfMonth,
                add_event_time_picker.hour,
                add_event_time_picker.minute
        )
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Snackbar.make(view, "Please select date in future", Snackbar.LENGTH_SHORT)
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
                eventLocationName = address.getAddressLine(0),
                eventLocationLatitude = address.latitude,
                eventLocationLongitude = address.longitude,
                eventLocationCountryCode = address.countryCode,
                eventDate = calendar.time,
                eventPlayers = eventPlayersNumber,
                eventDistance = null,
                eventSignedPlayers = eventSignedPlayers,
                eventComments = eventCommentMessage
        )
        addEventViewModel.createEvent(event)
    }

    companion object {
        private const val ARG_EVENT_ID = "EVENT_ID"

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
