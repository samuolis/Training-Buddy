package com.example.lukas.trainerapp.ui.viewmodel

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
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.Event
import com.example.lukas.trainerapp.webService.EventWebService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.lukas.trainerapp.db.entity.User
import com.google.android.gms.location.LocationServices
import java.util.*


class EventViewModel(application: Application) : AndroidViewModel(application) {

    var events: MutableLiveData<List<Event>>? = null
    var oneEvent: MutableLiveData<Event>? = MutableLiveData<Event>()
    var oneEventDashboard: MutableLiveData<Event>? = MutableLiveData<Event>()
    var eventsByLocation: MutableLiveData<List<Event>>? = null
    var refreshStatus: MutableLiveData<Int>? = null
    var refreshGlobalStatus: MutableLiveData<Int>? = null
    lateinit var eventWebService: EventWebService
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
        val gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        eventWebService = retrofit.create(EventWebService::class.java)
    }

    fun getEvents(): LiveData<List<Event>>? {
        if (events == null) {
            events = MutableLiveData<List<Event>>()
            loadEvents()
        }
        return events
    }

    fun getEventByPosition(): LiveData<Event>?{
        return oneEvent
    }

    fun loadOneEvent(position: Int? = null){
        if (position == null){
            oneEvent?.value = null
            return
        }
        var value = events?.value!![position]
        oneEvent?.value = value
    }

    fun getEventInDashboard(): LiveData<Event>?{
        return oneEventDashboard
    }

    fun loadOneEventInDashboard(position: Int? = null){
        if (position == null){
            oneEventDashboard?.value = null
            return
        }
        var value = eventsByLocation?.value!![position]
        oneEventDashboard?.value = value
    }

    fun getEventsOfLocation(): LiveData<List<Event>>? {
        if (eventsByLocation == null) {
            eventsByLocation = MutableLiveData<List<Event>>()
            loadEventsByLocation()
        }
        return eventsByLocation
    }

    fun loadEventsByLocation() {
        userPreferedDistance = userSharedPref?.getString(myApplication.getString(R.string.user_prefered_distance_key), "30")
        getDataFromLocation {deviceLocation, deviceCountryCode ->
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
        } else {
            Toast.makeText(myApplication, "You do not enabled location", Toast.LENGTH_LONG).show()
            callback(null, null)
        }
    }
}