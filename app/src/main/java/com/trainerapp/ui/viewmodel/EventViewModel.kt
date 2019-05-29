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
import com.trainerapp.models.CommentMessage
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

    private var _eventComments = MutableLiveData<List<CommentMessage>>()
    val eventComments = _eventComments.readOnly()

    private var _detailsOneEvent = MutableLiveData<Event>()
    val detailsOneEvent = _detailsOneEvent.readOnly()

    private var _loadingStatus = MutableLiveData<Int>()
    val loadingStatus = _loadingStatus.readOnly()

    private var _userEvents = MutableLiveData<List<Event>>()
    val userEvents = _userEvents.readOnly()

    private var _signedUsers = MutableLiveData<List<User>>()
    val signedUsers = _signedUsers.readOnly()

    private var _user = MutableLiveData<User>()
    val user = _user.readOnly()

    private var userPreferedDistance: String? = "30"
    private var userId: String? = null
    private var userSharedPref: SharedPreferences = myApplication
            .getSharedPreferences(myApplication
            .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
    private val schedulerUI = AndroidSchedulers.mainThread()

    init {
        userId = userSharedPref.getString(myApplication.getString(R.string.user_id_key), "0")
    }

    fun loadDetailsEvent(eventId: Long? = null) {
        if (eventId == null) {
            _detailsOneEvent.value = null
            return
        }
        eventWebService.getEventById(eventId).enqueue(object : Callback<Event> {
            override fun onFailure(call: Call<Event>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get event", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                if (response.isSuccessful) {
                    val value = response.body()
                    loadSignedUserList(value?.eventSignedPlayers)
                    loadEventComments(eventId)
                    _detailsOneEvent.value = value
                } else {
                    Toast.makeText(myApplication, "event id was wrong", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun loadEventsByLocation() {
        _refreshStatus.value = 1
        userPreferedDistance = userSharedPref.getString(myApplication.getString(R.string.user_prefered_distance_key), "30")

        locationService.getDeviceLocation()
                .subscribeOn(Schedulers.io())
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
                .subscribeOn(Schedulers.newThread())
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

    fun loadSignedUserList(userIdsList: List<String>?) {
        userWebService.getUserByIds(userIdsList).enqueue(object : Callback<List<User>> {

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    _signedUsers.value = response.body()

                } else {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun loadProfilePicture(pictureIndex: Int) {
        val userCache = _user.value
        userCache?.profilePictureIndex = pictureIndex
        _user.value = userCache
    }

    fun loadEventComments(eventId: Long? = null) {
        eventWebService.getEventById(eventId).enqueue(object : Callback<Event> {
            override fun onFailure(call: Call<Event>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get event", Toast.LENGTH_LONG).show()
                changeLoadStatus(0)
            }

            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                if (response.isSuccessful) {
                    querryComments(response.body())


                } else {
                    Toast.makeText(myApplication, "failed to get event", Toast.LENGTH_LONG).show()
                    changeLoadStatus(0)
                }
            }

        })
    }

    fun querryComments(event: Event?) {
        if (event?.eventComments != null) {
            eventWebService.getEventCommentsByIds(event.eventComments).enqueue(object : Callback<List<CommentMessage>> {
                override fun onFailure(call: Call<List<CommentMessage>>, t: Throwable) {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                    changeLoadStatus(0)
                }

                override fun onResponse(call: Call<List<CommentMessage>>, response: Response<List<CommentMessage>>) {
                    if (response.isSuccessful) {
                        _eventComments.value = response.body()
                        changeLoadStatus(0)

                    } else {
                        Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                        changeLoadStatus(0)
                    }
                }

            })
        } else {
            _eventComments.value = mutableListOf<CommentMessage>()
            changeLoadStatus(0)
        }
    }

    fun cleanComments() {
        _eventComments.value = mutableListOf()
    }

    // 0 - stop loading
    // 1 - start loading
    fun changeLoadStatus(status: Int) {
        _loadingStatus.value = status
    }
}
