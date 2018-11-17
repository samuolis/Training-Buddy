package com.example.lukas.trainerapp.db.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.model.UserData

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val user: LiveData<User>

    private var mUserData: MutableLiveData<UserData>? = null

    var mProfilePicture: MutableLiveData<Int>? = null

    private val database: AppDatabase



    val baseUrl: String
        get() = BASE_URL


    init {
        database = AppDatabase.getInstance(this.getApplication())
        Log.i(TAG, "getting user in viewmodel")
        user = database.userDao().user
        mUserData = MutableLiveData()
        mProfilePicture = MutableLiveData()
    }

    fun deleteAllUser() {

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

    fun init() {
        mUserData = MutableLiveData()
        mProfilePicture = MutableLiveData()
    }

    companion object {


        private val TAG = UserViewModel::class.java.simpleName
        val USER_ID_PREFERENCE = "userId"
        private val BASE_URL = "https://training-222106.appspot.com/"
    }
}
