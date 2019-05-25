package com.trainerapp.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    var events: MutableLiveData<List<Event>>? = null
    var archivedEvents: MutableLiveData<List<Event>>? = null
    var eventsByLocation: MutableLiveData<List<Event>>? = null
    var descriptionStatus: MutableLiveData<Int>? = MutableLiveData<Int>()
    var refreshStatus: MutableLiveData<Int>? = null
    var eventComments: MutableLiveData<List<CommentMessage>>? = MutableLiveData<List<CommentMessage>>()
    var myEventPosition: Int? = null
    var detailsEventId: Long? = null
    var detailsOneEvent: MutableLiveData<Event>? = MutableLiveData<Event>()
    var loadingStatus: MutableLiveData<Int>? = MutableLiveData<Int>()
    var loggedUser: FirebaseUser? = null

    var userEvents: MutableLiveData<List<Event>>? = null
    var signedUsersList: MutableLiveData<List<User>>? = MutableLiveData<List<User>>()

    private val _errorData: MutableLiveData<Throwable> = MutableLiveData()
    val errorData = _errorData.readOnly()

    var userWeb: MutableLiveData<User>? = null
    private var userPreferedDistance: String? = "30"
    var user: User? = null
    var userId: String? = null
    var userSharedPref: SharedPreferences = myApplication?.getSharedPreferences(myApplication
            .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
    val schedulerUI = AndroidSchedulers.mainThread()

    init {
        userId = userSharedPref?.getString(myApplication.getString(R.string.user_id_key), "0")
        loggedUser = FirebaseAuth.getInstance().currentUser
    }

    fun getEvents(): LiveData<List<Event>>? {
        if (events == null) {
            events = MutableLiveData<List<Event>>()
            loadEvents()
        }
        return events
    }

    fun getDetailsOneEvent(): LiveData<Event>?{
        return detailsOneEvent
    }

    fun setDescriptionStatus(status: Int){
        descriptionStatus?.value = status
    }

    fun loadDetailsEvent(eventId: Long? = null){
        if (eventId == null){
            detailsOneEvent?.value = null
            return
        }
        eventWebService.getEventById(eventId).enqueue(object : Callback<Event>{
            override fun onFailure(call: Call<Event>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get event", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                if (response.isSuccessful) {
                    var value = response.body()
                    loadSignedUserList(value?.eventSignedPlayers)
                    loadEventComments(eventId)
                    detailsOneEvent?.value = value
                    detailsEventId = value?.eventId

                } else {
                    Toast.makeText(myApplication, "event id was wrong", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun getEventsOfLocation(): LiveData<List<Event>>? {
        if (eventsByLocation == null) {
            eventsByLocation = MutableLiveData<List<Event>>()
            loadEventsByLocation()
        }
        return eventsByLocation
    }

    fun loadEventsByLocation() {
        refreshStatus?.value = 1
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
                            eventsByLocation?.value = eventsList
                            refreshStatus?.value = 0
                        },
                        onError = {
                            _errorData.value = it
                            refreshStatus?.value = 0
                        }
                ).bind()
    }

    fun getStatus(): MutableLiveData<Int>? {
        if (refreshStatus == null) {
            refreshStatus = MutableLiveData<Int>()
            refreshStatus?.value = 0
        }
        return refreshStatus
    }

    // 0 is for dashboard and 1 is for profile
    fun getStatusForDescription(): MutableLiveData<Int>? {
        if (descriptionStatus == null) {
            descriptionStatus = MutableLiveData<Int>()
            descriptionStatus?.value = 0
        }
        return descriptionStatus
    }

    fun getArchivedEvents(): LiveData<List<Event>>? {
        if (archivedEvents == null) {
            archivedEvents = MutableLiveData<List<Event>>()
            loadEvents()
        }
        return archivedEvents
    }

    fun loadEvents() {
        refreshStatus?.value = 1
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
                            events?.value = validDateEventList
                            archivedEvents?.value = archivedEventList
                            refreshStatus?.value = 0
                        },
                        onError = {
                            Toast.makeText(myApplication,
                                    "Failed to get data " + it.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                            refreshStatus?.value = 0
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


    fun getUserEvents(): LiveData<List<Event>>? {
        if (userEvents == null) {
            userEvents = MutableLiveData<List<Event>>()
            loadUserEventsByIds()
        }
        return userEvents
    }

    fun getUserWeb() : LiveData<User>? {
        if (userWeb == null) {
            userWeb = MutableLiveData<User>()
            loadUserData()
        }
        return userWeb
    }

    fun loadUserData() {
        refreshStatus?.value = 1
        var userSharedPref = myApplication?.getSharedPreferences(myApplication
                .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        var userId = userSharedPref?.getString(myApplication.getString(R.string.user_id_key), "0")
        userWebService.getExistantUser(userId)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            userWeb?.value = response.body()
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
        if (userWeb?.value?.signedEventsList == null){
            userEvents?.value = null
            refreshStatus?.value = 0
            return
        }

        eventWebService.getEventByIds(userWeb?.value?.signedEventsList)
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
                            userEvents?.value = validDateEventList
                            archivedEvents?.value = archivedEventList
                            refreshStatus?.value = 0
                        },
                        onError = {
                            Toast.makeText(myApplication,
                                    "Failed to get data " + it.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                            refreshStatus?.value = 0
                        }
                ).bind()
    }

    fun getSignedUsers(): LiveData<List<User>>?{
        return signedUsersList
    }

    fun loadSignedUserList(userIdsList: List<String>?){
        userWebService.getUserByIds(userIdsList).enqueue(object : Callback<List<User>>{

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    signedUsersList?.value = response.body()

                } else {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun loadProfilePicture(pictureIndex: Int){
        var userCache = userWeb?.value
        userCache?.profilePictureIndex = pictureIndex
        userWeb?.value = userCache
    }

    fun getEventComments(): LiveData<List<CommentMessage>>? {
        return eventComments
    }

    fun loadEventComments(eventId: Long? = null){
        eventWebService.getEventById(eventId).enqueue(object : Callback<Event>{
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

    fun querryComments(event: Event?){
        if (event?.eventComments != null) {
            eventWebService.getEventCommentsByIds(event?.eventComments).enqueue(object : Callback<List<CommentMessage>> {
                override fun onFailure(call: Call<List<CommentMessage>>, t: Throwable) {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                    changeLoadStatus(0)
                }

                override fun onResponse(call: Call<List<CommentMessage>>, response: Response<List<CommentMessage>>) {
                    if (response.isSuccessful) {
                        eventComments?.value = response.body()
                        changeLoadStatus(0)

                    } else {
                        Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                        changeLoadStatus(0)
                    }
                }

            })
        } else{
            eventComments?.value = mutableListOf<CommentMessage>()
            changeLoadStatus(0)
        }
    }

    fun cleanComments(){
        eventComments?.value = mutableListOf()
    }

    fun getLoadingStatus(): LiveData<Int>?{
        return loadingStatus
    }

    // 0 - stop loading
    // 1 - start loading
    fun changeLoadStatus(status: Int){
        loadingStatus?.value = status
    }
}
