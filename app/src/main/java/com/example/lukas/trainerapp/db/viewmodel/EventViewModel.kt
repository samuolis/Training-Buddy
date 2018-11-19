package com.example.lukas.trainerapp.db.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import com.example.lukas.trainerapp.db.entity.User


class EventViewModel(application: Application) : AndroidViewModel(application) {

    var events: MutableLiveData<List<Event>>? = null
    var eventsByLocation: MutableLiveData<List<Event>>? = null
    var userCountryCode: String? = "UK"
    var selectedUserLongitude: Float? =0.toFloat()
    var selectedUserLatitude: Float? =0.toFloat()
    var refreshStatus: MutableLiveData<Int>? = null
    var refreshGlobalStatus: MutableLiveData<Int>? = null
    lateinit var eventWebService: EventWebService
    lateinit var mDb: AppDatabase
    private val BASE_URL = "https://training-222106.appspot.com/"
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

    fun getEventsOfLocation(countryCode: String?, userLatitude: Float?, userLongitude: Float?): LiveData<List<Event>>? {
        if (eventsByLocation == null) {
            eventsByLocation = MutableLiveData<List<Event>>()
            userCountryCode = countryCode
            selectedUserLatitude = userLatitude
            selectedUserLongitude = userLongitude
            loadEventsByLocation(countryCode, userLatitude, userLongitude)
        }
        return eventsByLocation
    }

    fun loadEventsByLocation(countryCode: String? = userCountryCode, userLatitude: Float? = selectedUserLatitude,
                             userLongitude: Float? = selectedUserLongitude) {
        userPreferedDistance = userSharedPref?.getString(myApplication.getString(R.string.user_prefered_distance_key), "30")
        eventWebService.getEventsByLocation(userId, userPreferedDistance, countryCode,
                userLatitude, userLongitude).enqueue(object : Callback<List<Event>> {
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

    fun getStatus(): MutableLiveData<Int>? {
        if (refreshStatus == null) {
            refreshStatus = MutableLiveData<Int>()
            refreshStatus?.value = 0
        }
        return refreshStatus
    }

    fun getGlobalStatus(): MutableLiveData<Int>? {
        if (refreshGlobalStatus == null) {
            refreshGlobalStatus = MutableLiveData<Int>()
            refreshGlobalStatus?.value = 0
        }
        if (refreshGlobalStatus?.value == 1){

        }
        return refreshGlobalStatus
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
}