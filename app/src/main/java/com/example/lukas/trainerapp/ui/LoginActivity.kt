package com.example.lukas.trainerapp.ui

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment


import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel
import com.example.lukas.trainerapp.ui.fragments.LoginFragment
import com.example.lukas.trainerapp.ui.fragments.RegisterFragment
import com.facebook.CallbackManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    private val callbackManager: CallbackManager? = null
    private var actionBar: ActionBar? = null
    private var userViewModel: UserViewModel? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        actionBar = supportActionBar
        actionBar!!.setTitle(R.string.app_name)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        setContentView(R.layout.activity_login)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }
        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        val loginFragment = LoginFragment()
        val fragmentManager = supportFragmentManager
        // Begin the transaction
        fragmentManager.beginTransaction()
                .add(R.id.fragment_frame, loginFragment)
                .commit()
        // Replace the contents of the container with the new fragment
        //ft.replace(R.id.fragment_frame, new LoginFragment());

    }

    fun GoToRegisterFragment() {
        val registerFragment = RegisterFragment()
        val fragmentManager = supportFragmentManager
        actionBar!!.show()
        actionBar!!.title = "Fill registration"
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        // Begin the transaction
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_frame, registerFragment)
                .addToBackStack(null)
                .commit()
    }

    fun GoToNavigationActivity() {
        val myIntent = Intent(this@LoginActivity, NavigationActivity::class.java)
        startActivity(myIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                FirebaseAuth.getInstance().signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    supportFragmentManager.popBackStack()
                }
                return true
            }
        }

        return false
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        var frags = getSupportFragmentManager().getFragments();
//            if (frags != null) {
//                for (f in frags) {
//                    if (f != null)
//                        if (f is LoginFragment) { // custom interface with no signitures
//                            f.onActivityResult(requestCode, resultCode, data);
//                        }
//                }
//            }
//        }
}

