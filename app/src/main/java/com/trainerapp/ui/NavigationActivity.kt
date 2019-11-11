package com.trainerapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.trainerapp.R
import com.trainerapp.base.BaseActivity
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.extension.getViewModel
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class NavigationActivity : BaseActivity(), FragmentManager.OnBackStackChangedListener {

    private var doubleBackToExitPressedOnce = false
    lateinit var eventViewModel: EventViewModel
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    var fragmentAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        eventViewModel = getViewModel(viewModelFactory)
        EventBus.getDefault().register(this)

        supportActionBar?.title = getString(R.string.app_name)
        supportFragmentManager.addOnBackStackChangedListener(this)

        shouldDisplayHomeUp()
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, ContainerFragment())
                    .commit()
        }
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                permisionsResult = (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onBackStackChanged() {
        fragmentAdded = supportFragmentManager.backStackEntryCount > 0
        if (!fragmentAdded) {
            supportActionBar!!.title = getString(R.string.app_name)
        }
        shouldDisplayHomeUp()
        invalidateOptionsMenu()
    }

    fun getBackOnStackToMainMenu() {
        var fragmentSize = supportFragmentManager.backStackEntryCount
        while (fragmentSize > 0) {
            supportFragmentManager.popBackStack()
            fragmentSize -= 1
        }
    }

    private fun shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        supportActionBar!!.setDisplayHomeAsUpEnabled(fragmentAdded)
        supportActionBar!!.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
    }

    override fun onSupportNavigateUp(): Boolean {
        //This method is called when the up button is pressed. Just the pop back stack.
        supportFragmentManager.popBackStack()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!fragmentAdded) {
            menuInflater.inflate(R.menu.main, menu)
        }
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            CoroutineScope(Dispatchers.IO).launch {
                logout()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }


    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
                return
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    private fun logout() {
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        FirebaseInstanceId.getInstance().deleteInstanceId()
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val logoutIntent = Intent(this@NavigationActivity, LoginActivity::class.java)
            startActivity(logoutIntent)
            finish()
        }
    }

    fun backOnStack() {
        supportFragmentManager.popBackStack()
    }

    //register your activity onResume()
    public override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(BROADCAST_REFRESH))
    }

    //Must unregister onPause()
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    //This is the handler that will manager to process the broadcast intent
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(NOTIFICATION_EVENT_KEY)) {
                NOTIFICATION_EVENT_REFRESH_VALUE -> {
                    eventViewModel.loadEvents()
                    eventViewModel.loadEventsByLocation()
                    eventViewModel.loadUserEventsByIds()
                }
            }
        }
    }

    companion object {
        var permisionsResult: Boolean = false

        private const val REQUEST_PERMISION_CODE = 1111

        const val NOTIFICATION_EVENT_KEY = "notification_event"

        const val NOTIFICATION_EVENT_COMMENT_VALUE = "comment"

        const val NOTIFICATION_EVENT_REFRESH_VALUE = "refresh"

        const val EVENT_ID_INTENT: String = "EVENTID"

        const val BROADCAST_REFRESH: String = "refresh"
    }
}
