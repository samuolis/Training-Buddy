package com.trainerapp.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.trainerapp.R
import com.trainerapp.db.AppDatabase
import com.trainerapp.db.entity.Event
import com.trainerapp.web.webservice.EventWebService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.internal.BoltsMeasurementEventListener
import com.trainerapp.db.entity.User
import com.trainerapp.web.webservice.UserWebService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.trainerapp.models.CommentMessage
import java.util.*


class EventViewModel(application: Application) : AndroidViewModel(application) {

    var events: MutableLiveData<List<Event>>? = null
    var oneEventInHome: MutableLiveData<Event>? = MutableLiveData<Event>()
    var oneEventDashboard: MutableLiveData<Event>? = MutableLiveData<Event>()
    var eventsByLocation: MutableLiveData<List<Event>>? = null
    var descriptionStatus: MutableLiveData<Int>? = MutableLiveData<Int>()
    var profilePicture: MutableLiveData<Int>? = MutableLiveData<Int>()
    var refreshStatus: MutableLiveData<Int>? = null
    var eventComments: MutableLiveData<List<CommentMessage>>? = MutableLiveData<List<CommentMessage>>()
    var myEventPosition: Int? = null
    var detailsEventId: Long? = null
    var detailsOneEvent: MutableLiveData<Event>? = MutableLiveData<Event>()
    var loadingStatus: MutableLiveData<Int>? = MutableLiveData<Int>()
    var loggedUser: FirebaseUser? = null

    var userEvents: MutableLiveData<List<Event>>? = null
    var signedUsersList: MutableLiveData<List<User>>? = MutableLiveData<List<User>>()

    var userEventInProfile: MutableLiveData<Event>? = MutableLiveData<Event>()

    var userWeb: MutableLiveData<User>? = null

    lateinit var eventWebService: EventWebService
    lateinit var userWebService: UserWebService
    lateinit var mDb: AppDatabase
    val BASE_URL = "https://training-222106.appspot.com/"
    var userPreferedDistance: String? = "30"
    var user: User? = null
    var userId: String? = null
    private var myApplication = application
    lateinit var userSharedPref: SharedPreferences

    init {
        userSharedPref = myApplication?.getSharedPreferences(myApplication
                .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        userId = userSharedPref?.getString(myApplication.getString(R.string.user_id_key), "0")
        mDb = AppDatabase.getInstance(this.getApplication())
        loggedUser = FirebaseAuth.getInstance().currentUser
        val gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        eventWebService = retrofit.create(EventWebService::class.java)
        userWebService = retrofit.create(UserWebService::class.java)
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

//    fun loadDetailsOneEvent(){
//        when (descriptionStatus?.value) {
//            0 -> loadOneEventInDashboard()
//            1 -> loadOneEventInUserProfile()
//            2 -> loadOneEventInHome()
//        }
//        loadEventComments()
//    }

    // 0 -> Dashboard
    // 1 -> Profile
    // 2 -> Home
    fun setDescriptionStatus(status: Int){
        descriptionStatus?.value = status
    }

//    fun getOneEventInHome(): LiveData<Event>?{
//        return oneEventInHome
//    }
//
//    fun loadOneEventInHome(position: Int? = null){
//        if (myEventPosition != null){
//            var value = events?.value!![myEventPosition!!]
//            detailsOneEvent?.value = value
//            return
//        } else if (position == null){
//            detailsOneEvent?.value = null
//            return
//        }
//        myEventPosition = position
//        var value = events?.value!![position!!]
//        detailsOneEvent?.value = value
//    }

//    fun getEventInDashboard(): LiveData<Event>?{
//        return oneEventDashboard
//    }
//
//    fun loadOneEventInDashboard(position: Int? = null){
//        if (myEventPosition != null){
//            var value = eventsByLocation?.value!![myEventPosition!!]
//            detailsOneEvent?.value = value
//            return
//        } else if (position == null){
//            detailsOneEvent?.value = null
//            return
//        }
//        myEventPosition = position
//        var value = eventsByLocation?.value!![position]
//        detailsOneEvent?.value = value
//    }

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

//    fun getEventInProfile(): LiveData<Event>?{
//        return userEventInProfile
//    }
//
//    fun loadOneEventInUserProfile(position: Int? = null){
//        if (myEventPosition != null){
//            var value = userEvents?.value!![myEventPosition!!]
//            detailsOneEvent?.value = value
//            return
//        } else if (position == null){
//            detailsOneEvent?.value = null
//            return
//        }
//        myEventPosition = position
//        var value = userEvents?.value!![position]
//        detailsOneEvent?.value = value
//    }

    fun getEventsOfLocation(): LiveData<List<Event>>? {
        if (eventsByLocation == null) {
            eventsByLocation = MutableLiveData<List<Event>>()
            loadEventsByLocation()
        }
        return eventsByLocation
    }

    fun loadEventsByLocation() {
        refreshStatus?.value = 1
        userPreferedDistance = userSharedPref?.getString(myApplication.getString(R.string.user_prefered_distance_key), "30")
        getDataFromLocation {deviceLocation, deviceCountryCode ->
            if (deviceLocation == null || deviceCountryCode == null){
                eventsByLocation = null
                refreshStatus?.value = 0
                return@getDataFromLocation
            }
            eventWebService.getEventsByLocation(userId, userPreferedDistance, deviceCountryCode,
                    deviceLocation?.latitude?.toFloat(),
                    deviceLocation?.longitude?.toFloat())
                    .enqueue(object : Callback<List<Event>> {
                override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    Toast.makeText(myApplication, "failure", Toast.LENGTH_LONG).show()
                    refreshStatus?.value = 0
                }

                override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                    eventsByLocation?.value = response.body()
                    refreshStatus?.value = 0
                }

            })
        }
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

    fun loadEvents() {
        refreshStatus?.value = 1
        eventWebService.getEventsByUserId(userId = userId).enqueue(object : Callback<List<Event>>{
            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Toast.makeText(myApplication, "failure", Toast.LENGTH_LONG).show()
                refreshStatus?.value = 0
            }

            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful()){
                    events?.value = response.body()
                    refreshStatus?.value = 0
                } else{
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    fun getDataFromLocation (callback: (gotLocation: Location?, countryCode: String?) -> Unit){
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(myApplication)
        if (ContextCompat.checkSelfPermission(myApplication,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null)
                        {
                            var geocoder = Geocoder(myApplication, Locale.getDefault())
                            var adresses = geocoder.getFromLocation(location.latitude,
                                    location.longitude, 1)
                            var address = adresses[0]
                            callback(location, address.countryCode)
                        }
                    }
                    .addOnFailureListener {exception ->
                        Toast.makeText(myApplication, "Failed to load location with : " + exception.message, Toast.LENGTH_LONG).show()
                        callback(null, null)
                    }
        } else {
            Toast.makeText(myApplication, "You do not enabled location", Toast.LENGTH_LONG).show()
            callback(null, null)
        }
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
        }
        eventWebService.getEventByIds(userWeb?.value?.signedEventsList)
                .enqueue(object : Callback<List<Event>> {
                    override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                        refreshStatus?.value = 0
                    }

                    override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                        if (response.isSuccessful) {
                            getDataFromLocation { deviceLocation, deviceCountryCode ->
                                if (deviceLocation == null || deviceCountryCode == null) {
                                    eventsByLocation = null
                                    refreshStatus?.value = 0
                                    return@getDataFromLocation
                                }
                                var userEventsList = response.body()
                                var newUserEventList = mutableListOf<Event>()
                                userEventsList?.forEach {
                                    var newEvent = it
                                    var eventLoacation = Location("Event")
                                    eventLoacation.longitude = newEvent.eventLocationLongitude!!
                                    eventLoacation.latitude = newEvent.eventLocationLatitude!!
                                    var distance = eventLoacation.distanceTo(deviceLocation)/1000
                                    newEvent.eventDistance = distance
                                    newUserEventList.add(newEvent)
                                }

                                userEvents?.value = newUserEventList
                                refreshStatus?.value = 0
                            }

                        } else {
                            Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                            refreshStatus?.value = 0
                        }
                    }

                })
    }

    fun getProfilePicture(): LiveData<Int>?{
        return profilePicture
    }

    fun getSignedUsers(): LiveData<List<User>>?{
        return signedUsersList
    }

    fun loadSignedUserList(userIdsList: List<String>?){
        userWebService.getUserByIds(userIdsList).enqueue(object : Callback<List<User>>{

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                loadingStatus?.value = 0
            }

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    signedUsersList?.value = response.body()
                    loadingStatus?.value = 0

                } else {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                    loadingStatus?.value = 0
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
            }

            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                if (response.isSuccessful) {
                    querryComments(response.body())

                } else {
                    Toast.makeText(myApplication, "failed to get event", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun querryComments(event: Event?){
        if (event?.eventComments != null) {
            eventWebService.getEventCommentsByIds(event?.eventComments).enqueue(object : Callback<List<CommentMessage>> {
                override fun onFailure(call: Call<List<CommentMessage>>, t: Throwable) {
                    Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<List<CommentMessage>>, response: Response<List<CommentMessage>>) {
                    if (response.isSuccessful) {
                        eventComments?.value = response.body()

                    } else {
                        Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                    }
                }

            })
        } else{
            eventComments?.value = mutableListOf<CommentMessage>()
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