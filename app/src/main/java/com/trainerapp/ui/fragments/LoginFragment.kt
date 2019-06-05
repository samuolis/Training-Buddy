package com.trainerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.extension.nonNullObserve
import com.trainerapp.ui.LoginActivity
import com.trainerapp.ui.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject


class LoginFragment : BaseFragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var loginViewModel: LoginViewModel

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

    override fun onInject(activityComponent: ActivityComponent) {
        super.onInject(activityComponent)
        activityComponent.inject(this)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = getViewModel(viewModelFactory)
        login_email!!.setOnClickListener {
            showProgressBar()
            signIn()
        }

        loginViewModel.error.nonNullObserve(this) {
            hideProgressBar()
        }
        login_email.setSize(SignInButton.SIZE_WIDE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                loginViewModel.firebaseAuthWithGoogle(account!!)
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

    companion object {

        private val RC_SIGN_IN = 11

        private val TAG = LoginFragment::class.java.simpleName
    }

}
