package com.example.lukas.trainerapp.ui.fragments


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.AppExecutors
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel
import com.example.lukas.trainerapp.enums.ProfilePicture
import com.example.lukas.trainerapp.webService.UserWebService
import com.example.lukas.trainerapp.ui.NavigationActivity
import com.example.lukas.trainerapp.utils.DrawableUtils
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_account_edit.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class AccountEditDialogFragment : DialogFragment() {

    lateinit var userViewModel : UserViewModel
    lateinit var mDb: AppDatabase
    var userId: String? = null
    var databaseId: Long = 0
    var profileInt: Int? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        // Inflate the layout to use as dialog or embedded fragment
        var rootView = inflater.inflate(R.layout.fragment_account_edit, container, false)
        mDb = AppDatabase.getInstance(activity)
        rootView.post({
            userViewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
            userViewModel.user.observe(this, Observer { user: User ->
                profileInt = user.profilePictureIndex
                if (user.profilePictureIndex == null || user.profilePictureIndex!! >= ProfilePicture.values().size){
                    DrawableUtils.setupInitials(initials_image_view_fragment_edit, user)
                } else{
                    initials_image_view_fragment_edit.setImageResource(ProfilePicture.values()[user.profilePictureIndex!!].drawableId)
                }
                name_edit_text.text = SpannableStringBuilder(user.fullName)
                phone_edit_text.text = SpannableStringBuilder(user.phoneNumber)
                email_edit_text.text = SpannableStringBuilder(user.email)
                userId = user.userId
                databaseId = user.id
                edit_profile_submit_text_view.setOnClickListener {
                    submitEdit()
                }
                initials_image_view_fragment_edit.setOnClickListener {
                    (activity as NavigationActivity).showProfilePictureDialogFragment()
                }
                edit_profile_default_picture_text_view.setOnClickListener {
                    profileInt = ProfilePicture.values().size
                    DrawableUtils.setupInitials(initials_image_view_fragment_edit, user)
                }
            })
        })
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    fun submitEdit(){
        // Reset errors.
        name_edit_text.setError(null)
        email_edit_text.setError(null)
        phone_edit_text.setError(null)

        // Store values at the time of the login attempt.
        val email = email_edit_text.getText().toString()
        val phoneNumber = phone_edit_text.getText().toString()
        val fullName = name_edit_text.getText().toString()

        var cancel = false
        var focusView: View? = null


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            email_edit_text.setError(getString(R.string.error_field_required))
            focusView = email_edit_text
            cancel = true
        } else if (!isEmailValid(email)) {
            email_edit_text.setError(getString(R.string.error_invalid_email))
            focusView = email_edit_text
            cancel = true
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phone_edit_text.setError(getString(R.string.error_field_required))
            focusView = phone_edit_text
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
            val user = User(databaseId, userId, fullName, email, phoneNumber, currentTime, profileInt)

            val gson = GsonBuilder()
                    .setLenient()
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(userViewModel.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

            val userWebService = retrofit.create(UserWebService::class.java)

            userWebService.postUser(user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    AppExecutors.getInstance().diskIO().execute {
                        mDb.userDao().insertUser(user)
                        (activity as NavigationActivity).backOnStack()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(activity, t.localizedMessage, Toast.LENGTH_LONG).show()
                }
            })

        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }
}
