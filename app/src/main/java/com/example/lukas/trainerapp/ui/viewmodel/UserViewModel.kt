package com.example.lukas.trainerapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lukas.trainerapp.AppExecutors
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.Event
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.model.UserData
import com.example.lukas.trainerapp.webService.EventWebService
import com.example.lukas.trainerapp.webService.UserWebService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val user: LiveData<User>

    var userEvents: MutableLiveData<List<Event>>? = null

    var userWeb: MutableLiveData<User>? = null

    private var mUserData: MutableLiveData<UserData>? = null

    var mProfilePicture: MutableLiveData<Int>? = null

    private val database: AppDatabase

    private var myApplication = application

    lateinit var userWebService: UserWebService

    lateinit var eventWebService: EventWebService



    val baseUrl: String
        get() = BASE_URL


    init {
        database = AppDatabase.getInstance(this.getApplication())
        Log.i(TAG, "getting user in viewmodel")
        user = database.userDao().user
        mUserData = MutableLiveData()
        mProfilePicture = MutableLiveData()
        val gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        userWebService = retrofit.create(UserWebService::class.java)
        eventWebService = retrofit.create(EventWebService::class.java)
    }

    fun getmUserData(): UserData? {
        return mUserData!!.value
    }

    fun setmUserData(mUserData: UserData) {
        this.mUserData!!.value = mUserData
    }

    fun getmProfilePicture(): Int? {
        return mProfilePicture!!.value
    }

    fun setmProfilePicture(index: Int?) {
        this.mProfilePicture!!.value = index
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
        var userSharedPref = myApplication?.getSharedPreferences(myApplication
                .getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        var userId = userSharedPref?.getString(myApplication.getString(R.string.user_id_key), "0")
        userWebService.getExistantUser(userId)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            userWeb?.value = response.body()

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
        eventWebService.getEventByIds(userWeb?.value?.signedEventsList)
                .enqueue(object : Callback<List<Event>> {
                    override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    }

                    override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                        if (response.isSuccessful) {
                            userEvents?.value = response.body()

                        } else {
                            Toast.makeText(myApplication, "failed to get data", Toast.LENGTH_LONG).show()
                        }
                    }

                })
    }

    companion object {


        private val TAG = UserViewModel::class.java.simpleName
        val USER_ID_PREFERENCE = "userId"
        private val BASE_URL = "https://training-222106.appspot.com/"
    }
}
