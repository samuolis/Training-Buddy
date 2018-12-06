package com.example.lukas.trainerapp.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.lukas.trainerapp.AppExecutors
import com.example.lukas.trainerapp.ui.LoginActivity
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel
import com.example.lukas.trainerapp.model.UserData
import com.example.lukas.trainerapp.web.webservice.UserWebService
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.util.Calendar
import java.util.Date

import android.Manifest.permission.READ_CONTACTS
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : Fragment() {

    private var mDb: AppDatabase? = null
    private var userViewModel: UserViewModel? = null
    var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_register, container, false)
        userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        mDb = AppDatabase.getInstance(context)
        rootView.post {

            register_button!!.setOnClickListener { v: View -> attemptRegister() }
            fillInformation()

        }

        return rootView
    }

    private fun fillInformation() {

        user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val userName = it.displayName
            val userEmail = it.email
            val photoUrl = it.photoUrl
            full_name!!.setText(userName)
            email!!.setText(userEmail)
            if (userEmail != null){
                email.isEnabled = false
            }

            // Check if user's email is verified
            val emailVerified = it.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = it.uid


        }
    }

    private fun attemptRegister() {

        // Reset errors.
        email!!.error = null

        // Store values at the time of the login attempt.
        val emailString = email!!.text.toString()
        val phoneNumber = phone_number_edittext!!.text.toString()
        val fullName = full_name!!.text.toString()

        var cancel = false
        var focusView: View? = null


        // Check for a valid email address.
        if (TextUtils.isEmpty(emailString)) {
            email!!.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailString)) {
            email!!.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            val currentTime = Calendar.getInstance().time
            val userId = user?.uid
            if (userId == null || userId == ""){
                return
            }

            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("LoginFragment", "getInstanceId failed", task.exception)
                        } else {

                            // Get new Instance ID token
                            val token = task.result!!.token

                            val user = User(userId, fullName, emailString, phoneNumber,
                                    currentTime, null, null, token)

                            val gson = GsonBuilder()
                                    .setLenient()
                                    .create()

                            val retrofit = Retrofit.Builder()
                                    .baseUrl(userViewModel!!.baseUrl)
                                    .addConverterFactory(GsonConverterFactory.create(gson))
                                    .build()

                            val userWebService = retrofit.create(UserWebService::class.java)

                            userWebService.postUser(user).enqueue(object : Callback<User> {
                                override fun onResponse(call: Call<User>, response: Response<User>) {
                                    AppExecutors.getInstance().diskIO().execute {
                                        try {
                                            mDb!!.userDao().insertUser(response.body()!!)
                                            (activity as LoginActivity).GoToNavigationActivity()
                                        } catch (e: Exception) {
                                            Toast.makeText(activity, "failed to store data", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<User>, t: Throwable) {
                                    Toast.makeText(activity, t.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                            })

                        }
                    }

        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

}
