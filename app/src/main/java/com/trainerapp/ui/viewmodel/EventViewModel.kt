package com.trainerapp.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.trainerapp.R
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.readOnly
import com.trainerapp.models.Event
import com.trainerapp.models.User
import com.trainerapp.service.LocationService
import com.trainerapp.web.webservice.EventWebService
import com.trainerapp.web.webservice.UserWebService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject


class EventViewModel @Inject constructor(
        private val myApplication: Activity,
        private val eventWebService: EventWebService,
        private val userWebService: UserWebService,
        private val locationService: LocationService
) : BaseViewModel() {

    private val _errorData: MutableLiveData<Throwable> = MutableLiveData()
    val errorData = _errorData.readOnly()

    private var _refreshStatus: MutableLiveData<Int> = MutableLiveData()
    val refreshStatus = _refreshStatus.readOnly()

    private var _events = MutableLiveData<List<Event>>()
    val events = _events.readOnly()

    private var _archivedEvents = MutableLiveData<List<Event>>()
    val archivedEvents = _archivedEvents.readOnly()

    private var _eventsByLocation = MutableLiveData<List<Event>>()
    val eventsByLocation = _eventsByLocation.readOnly()

    private var _userEvents = MutableLiveData<List<Event>>()
    val userEvents = _userEvents.readOnly()

    private var _user = MutableLiveData<User>()
    val user = _user.readOnly()

    private var userPreferedDistance: String? = "30"
    private var userId: String? = null
    private var userSharedPref: SharedPreferences = myApplication
            .getSharedPreferences(myApplication
            .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
    private val schedulerUI = AndroidSchedulers.mainThread()
    private val schedulerIO = Schedulers.io()

    init {
        userId = userSharedPref.getString(myApplication.getString(R.string.user_id_key), "0")
    }

    fun loadEventsByLocation() {
        _refreshStatus.value = 1
        userPreferedDistance = userSharedPref.getString(myApplication.getString(R.string.user_prefered_distance_key), "30")

        locationService.getDeviceLocation()
                .subscribeOn(schedulerIO)
                .observeOn(schedulerUI)
                .flatMap { location ->
                    val geocoder = Geocoder(myApplication, Locale.getDefault())
                    val adresses = geocoder.getFromLocation(location.latitude,
                            location.longitude, 1)
                    eventWebService.getEventsByLocation(
                            userId,
                            userPreferedDistance,
                            adresses[0].countryCode,
                            location.latitude.toFloat(),
                            location.longitude.toFloat()
                    )
                            .subscribeOn(Schedulers.io())
                            .observeOn(schedulerUI)
                }
                .subscribeBy(
                        onSuccess = { eventsList ->
                            _eventsByLocation.value = eventsList
                            _refreshStatus.value = 0
                        },
                        onError = {
                            _errorData.value = it
                            _refreshStatus.value = 0
                        }
                ).bind()
    }

    fun loadEvents() {
        _refreshStatus.value = 1
        eventWebService.getEventsByUserId(userId = userId)
                .subscribeOn(schedulerIO)
                .observeOn(schedulerUI)
                .zipWith(locationService.getDeviceLocation(),
                        BiFunction { eventsList: List<Event>, location: Location ->
                            updateEventsList(eventsList, location)
                        })
                .subscribeBy(
                        onSuccess = { eventsList ->
                            val validDateEventList = eventsList.filter {
                                it.eventDate!!.after(Date(System.currentTimeMillis()))
                            }
                            val archivedEventList = eventsList.filter {
                                it.eventDate!!.before(Date(System.currentTimeMillis()))
                            }
                            _events.value = validDateEventList
                            _archivedEvents.value = archivedEventList
                            _refreshStatus.value = 0
                        },
                        onError = {
                            Toast.makeText(myApplication,
                                    "Failed to get data " + it.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                            _refreshStatus.value = 0
                        }
                ).bind()
    }

    private fun updateEventsList(eventsList: List<Event>, location: Location): List<Event> {
        val newUserEventList = mutableListOf<Event>()
        eventsList.forEach {
            val newEvent = it
            val eventLoacation = Location("Event")
            eventLoacation.longitude = newEvent.eventLocationLongitude!!
            eventLoacation.latitude = newEvent.eventLocationLatitude!!
            val distance = eventLoacation.distanceTo(location) / 1000
            newEvent.eventDistance = distance
            newUserEventList.add(newEvent)
        }
        return newUserEventList
    }

    fun loadUserData() {
        _refreshStatus.value = 1
        val userSharedPref = myApplication.getSharedPreferences(myApplication
                .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        val userId = userSharedPref?.getString(myApplication.getString(R.string.user_id_key), "0")
        userWebService.getExistantUser(userId)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            _user.value = response.body()
                            loadUserEventsByIds()

                        } else {
                            Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(myApplication, t.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                })
    }

    fun loadUserEventsByIds() {
        if (_user.value?.signedEventsList == null) {
            _userEvents.value = null
            _refreshStatus.value = 0
            return
        }

        eventWebService.getEventByIds(_user.value?.signedEventsList)
                .zipWith(locationService.getDeviceLocation(),
                        BiFunction { eventsList: List<Event>, location: Location ->
                            updateEventsList(eventsList, location)
                        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(schedulerUI)
                .subscribeBy(
                        onSuccess = { eventsList ->
                            val validDateEventList = eventsList.filter {
                                it.eventDate!!.after(Date(System.currentTimeMillis()))
                            }
                            val archivedEventList = eventsList.filter {
                                it.eventDate!!.before(Date(System.currentTimeMillis()))
                            }
                            _userEvents.value = validDateEventList
                            _archivedEvents.value = archivedEventList
                            _refreshStatus.value = 0
                        },
                        onError = {
                            Toast.makeText(myApplication,
                                    "Failed to get data " + it.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                            _refreshStatus.value = 0
                        }
                ).bind()
    }

    fun loadProfilePicture(pictureIndex: Int) {
        val userCache = _user.value
        userCache?.profilePictureIndex = pictureIndex
        _user.value = userCache
    }
}
