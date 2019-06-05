package com.trainerapp.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.R
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.toSingle
import com.trainerapp.models.User
import com.trainerapp.ui.LoginActivity
import com.trainerapp.web.webservice.UserWebService
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class LoginViewModel @Inject constructor(
        private val activity: Activity,
        private val userWebService: UserWebService
) : BaseViewModel() {

    private val TAG = LoginViewModel::class.simpleName
    private val schedulersIO = Schedulers.io()
    private val sharedPref = activity.getSharedPreferences(
            activity.getString(R.string.user_id_preferences),
            Context.MODE_PRIVATE
    )
    private val editor = sharedPref.edit()

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential).toSingle()
                .subscribeOn(schedulersIO)
                .flatMap { authResult: AuthResult ->
                    val user = authResult.user
                    Log.d(TAG, "signInWithCredential:success with user : " + user!!.email)
                    editor.putString(activity.getString(R.string.user_id_key), user.uid)
                    editor.apply()
                    checkUserOrCreate(user)
                }
                .subscribeBy(
                        onSuccess = { user ->
                            subscribeToAllUserEvents(user)
                            (activity as LoginActivity).GoToNavigationActivity()
                            FirebaseMessaging.getInstance().subscribeToTopic("all")
                        },
                        onError = {
                            Log.w(TAG, "signInWithCredential:failure", it)
                            _error.value = it

                        }
                ).bind()
    }

    private fun checkUserOrCreate(user: FirebaseUser): Single<User> {
        return userWebService.getExistantUser(user.uid)
                .subscribeOn(schedulersIO)
                .onErrorResumeNext {
                    val newUser = User(
                            userId = user.uid,
                            fullName = user.displayName,
                            createdAt = Calendar.getInstance().time,
                            profilePictureIndex = null,
                            signedEventsList = null
                    )
                    userWebService.postUser(newUser)
                            .subscribeOn(schedulersIO)
                }
    }

    private fun subscribeToAllUserEvents(user: User) {
        user.signedEventsList?.forEach {
            FirebaseMessaging.getInstance().subscribeToTopic(it.toString())
        } ?: return
    }
}
