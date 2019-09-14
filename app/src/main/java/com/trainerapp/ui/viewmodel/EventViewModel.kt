package com.trainerapp.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.trainerapp.R
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.readOnly
import com.trainerapp.models.Event
import com.trainerapp.models.User
import com.trainerapp.service.LocationService
import com.trainerapp.service.PermissionService
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.web.webservice.EventWebService
import com.trainerapp.web.webservice.UserWebService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject


class EventViewModel @Inject constructor(
        private val activity: Activity,
        private val eventWebService: EventWebService,
        private val userWebService: UserWebService,
        private val locationService: LocationService,
        permissionService: PermissionService
) : BaseViewModel() {

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
    private var userSharedPref: SharedPreferences = activity
            .getSharedPreferences(activity
            .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
    private val schedulerUI = AndroidSchedulers.mainThread()
    private val schedulerIO = Schedulers.io()

    init {
        userId = userSharedPref.getString(activity.getString(R.string.user_id_key), "0")
        permissionService.checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribeBy(
                        onSuccess = {
                            val test = it
                        },
                        onError = {
                            val test = it
                        }
                )
                .bind()
    }

    fun loadEventsByLocation() {
        _refreshStatus.value = 1
        userPreferedDistance = userSharedPref.getString(activity.getString(R.string.user_prefered_distance_key), "30")

        locationService.getDeviceLocation()
                .subscribeOn(schedulerIO)
                .observeOn(schedulerUI)
                .flatMap { location ->
                    val geocoder = Geocoder(activity, Locale.getDefault())
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
                            _error.value = it
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
                            _error.value = it
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
        userWebService.getExistantUser(userId)
                .subscribeOn(schedulerIO)
                .subscribeBy(
                        onSuccess = { user ->
                            _user.value = user
                        },
                        onError = {
                            _error.value = it
                            _refreshStatus.value = 0
                        }
                ).bind()
    }

    fun loadUserEventsByIds() {
        userWebService.getExistantUser(userId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(schedulerUI)
                .flatMap { user ->
                    _user.value = user
                    eventWebService.getUserSignedEvents(user.userId)
                            .subscribeOn(Schedulers.io())
                }
                .zipWith(locationService.getDeviceLocation(),
                        BiFunction { eventsList: List<Event>, location: Location ->
                            updateEventsList(eventsList, location)
                        })
                .observeOn(schedulerUI)
                .subscribeBy(
                        onSuccess = { eventsList ->
                            val validDateEventList = eventsList.filter {
                                it.eventDate!!.after(Date(System.currentTimeMillis()))
                            }
                            _userEvents.value = validDateEventList
                            _refreshStatus.value = 0
                        },
                        onError = {
                            _error.value = it
                            _refreshStatus.value = 0
                        }
                ).bind()
    }

    fun loadProfilePicture(pictureIndex: Int) {
        val userCache = _user.value
        userCache?.profilePictureIndex = pictureIndex
        _user.value = userCache
    }

    fun postUser(user: User) {
        userWebService.postUser(user)
                .subscribeOn(schedulerIO)
                .subscribeBy(
                        onSuccess = {
                            (activity as NavigationActivity).backOnStack()
                        },
                        onError = {
                            _error.value = it
                        }
                ).bind()
    }
}
