package com.trainerapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.trainerapp.R
import com.trainerapp.models.User
import com.trainerapp.ui.LoginActivity
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.web.webservice.UserWebService
import kotlinx.android.synthetic.main.fragment_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class LoginFragment : Fragment() {

    private var userWebService: UserWebService? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    lateinit var userSharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var eventViewModel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(activity!!, gso)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)
        eventViewModel = ViewModelProviders.of(activity!!).get(EventViewModel::class.java)
        val gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(eventViewModel!!.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        userWebService = retrofit.create(UserWebService::class.java)
        userSharedPref = activity!!.getSharedPreferences(context
                ?.getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
        editor = userSharedPref.edit()

        rootView.post {

            login_email!!.setOnClickListener {
                showProgressBar()
                signIn()
            }

            login_email.setSize(SignInButton.SIZE_WIDE)
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                hideProgressBar()
                Snackbar.make(login_layout, "Google sign in failed." + e.message, Snackbar.LENGTH_SHORT).show()

            }
        } else {
            hideProgressBar()
        }


    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        val user = auth.currentUser
                        Log.d(TAG, "signInWithCredential:success with user : " + user!!.email)
                        val sharedPref = activity!!.getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString(getString(R.string.user_id_key), user.uid)
                        editor.commit()
                        getUser(user)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        hideProgressBar()
                        Snackbar.make(login_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    }

                }
    }

    private fun getUser(gotUser: FirebaseUser) {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    userWebService!!.getExistantUser(gotUser.uid)
                            .enqueue(object : Callback<User> {
                                override fun onResponse(call: Call<User>, response: Response<User>) {
                                    if (response.isSuccessful) {
                                        if (!task.isSuccessful) {
                                            hideProgressBar()
                                            Log.w("LoginFragment", "getInstanceId failed",
                                                    task.exception)
                                        } else {
                                            subscribeToAllUserEvents(response.body()!!)
                                            onSuccesfullLogin()
                                        }
                                    } else {
                                        newUserCreation(gotUser)
                                    }
                                }

                                override fun onFailure(call: Call<User>, t: Throwable) {
                                    newUserCreation(gotUser)
                                }
                            })
                }
    }

    private fun newUserCreation(user: FirebaseUser) {
        val newUser = User(user.uid, user.displayName, Calendar.getInstance().time,
                null, null)
        userWebService!!.postUser(newUser!!).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                onSuccesfullLogin()
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                hideProgressBar()
                Snackbar.make(login_layout, "Try again.", Snackbar.LENGTH_SHORT).show()
            }
        })
    }


    private fun showProgressBar() {
        login_progress!!.visibility = View.VISIBLE
        progress_bar_background!!.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        login_progress!!.visibility = View.GONE
        progress_bar_background!!.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser?.uid != null) {
            onSuccesfullLogin()
        }
    }

    fun onSuccesfullLogin() {
        (activity as LoginActivity).GoToNavigationActivity()
        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }

    fun subscribeToAllUserEvents(user: User) {
        val signedEvents = user.signedEventsList
        if (signedEvents != null) {
            signedEvents.forEach {
                FirebaseMessaging.getInstance().subscribeToTopic(it.toString())
            }
        }
    }

    companion object {

        private val RC_SIGN_IN = 11

        private val TAG = LoginFragment::class.java.simpleName
    }

}
