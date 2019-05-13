package com.trainerapp.ui

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.trainerapp.R
import com.trainerapp.base.BaseActivity
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.ui.fragments.*
import com.trainerapp.ui.viewmodel.EventViewModel
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class NavigationActivity : BaseActivity(), FragmentManager.OnBackStackChangedListener {

    var profileFragment = ProfileFragment()
    var homeFragment = HomeFragment()
    var dashboardFragment = DashboardFragment()

    private val REQUEST_PERMISION_CODE = 1111

    companion object {
        var permisionsResult: Boolean = false

        val NOTIFICATION_EVENT_KEY = "notification_event"

        val NOTIFICATION_EVENT_COMMENT_VALUE = "comment"

        val NOTIFICATION_EVENT_REFRESH_VALUE = "refresh"

        val EVENT_ID_INTENT: String = "EVENTID"

        fun refreshData(context: Context, eventKey: String, eventId: String) {

            val intent = Intent("refresh")
            intent.putExtra(EVENT_ID_INTENT, eventId)
            intent.putExtra(NOTIFICATION_EVENT_KEY, eventKey)

            context.sendBroadcast(intent)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                eventViewModel.setDescriptionStatus(2)
                eventViewModel.loadEvents()
                supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.navigation_frame, homeFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                eventViewModel.setDescriptionStatus(0)
                eventViewModel.loadEventsByLocation()
                supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.navigation_frame, dashboardFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                eventViewModel.setDescriptionStatus(1)
                eventViewModel.loadUserData()
                supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.navigation_frame, profileFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private var doubleBackToExitPressedOnce = false
    lateinit var eventViewModel: EventViewModel
    lateinit var logoutIntent: Intent
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    var fragmentAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        eventViewModel = ViewModelProviders.of(this).get(EventViewModel::class.java)

        supportActionBar?.title = getString(R.string.app_name)
        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISION_CODE /*LOCATION_PERMISSION_REQUEST_CODE*/)
        }
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp()
        if (supportFragmentManager.backStackEntryCount == 0) {
            eventViewModel.setDescriptionStatus(2)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.navigation_frame, homeFragment)
                    .commit()
            eventViewModel.loadEvents()
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
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

    fun shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        supportActionBar!!.setDisplayHomeAsUpEnabled(fragmentAdded)
        supportActionBar!!.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
    }

    override fun onSupportNavigateUp(): Boolean {
        //This method is called when the up button is pressed. Just the pop back stack.
        supportFragmentManager.popBackStack()
        if (supportFragmentManager.backStackEntryCount < 2) {
            if (eventViewModel.myEventPosition != null) {
                eventViewModel.myEventPosition = null
            }
            if (eventViewModel.eventComments != null) {
                eventViewModel.cleanComments()
            }
        }
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
                Logout()
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
            if (supportFragmentManager.backStackEntryCount < 2) {
                cleanCashedData()
            }
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

    fun cleanCashedData() {
        if (eventViewModel.myEventPosition != null) {
            eventViewModel.myEventPosition = null
        }
        if (eventViewModel.eventComments != null) {
            eventViewModel.cleanComments()
        }
    }

    fun Logout() {
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        FirebaseInstanceId.getInstance().deleteInstanceId()
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            logoutIntent = Intent(this@NavigationActivity, LoginActivity::class.java)
            startActivity(logoutIntent)
            finish()
        }
    }

    fun showAccountEditDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = AccountEditDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.edit_acount_title)
    }

    fun showEventCreateDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = AddEventDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.create_new_event_title)
    }

    fun showEventSignedUsersListDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = EventSignedUsersListDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.signed_user_list_title)
    }

    fun showEventCommentsDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = EventCommentsDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.event_comments)
    }

    fun showDashnoardSearchDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = SearchFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.search_properties_title)
    }

    fun showProfilePictureDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = ProfilePictureDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
    }

    fun showEventDetailsDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = EventDetailsDialogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
    }

    fun showArchivedEventsDialogFragment() {
        val fragmentManager = supportFragmentManager
        val newFragment = ArchivedEventsDIalogFragment()
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager.beginTransaction()
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
        supportActionBar!!.title = getString(R.string.archived_events)
    }

    fun backOnStack() {
        supportFragmentManager.popBackStack()
    }

    //register your activity onResume()
    public override fun onResume() {
        super.onResume()
        registerReceiver(mMessageReceiver, IntentFilter("refresh"))
    }

    //Must unregister onPause()
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mMessageReceiver)
    }

    //This is the handler that will manager to process the broadcast intent
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var eventKey = intent.getStringExtra(NOTIFICATION_EVENT_KEY)
            when (eventKey) {
                NOTIFICATION_EVENT_COMMENT_VALUE -> {
                    var eventId = intent.getStringExtra(EVENT_ID_INTENT)
                    if (eventViewModel.detailsEventId != null && eventViewModel.detailsEventId == eventId.toLong()) {
                        eventViewModel.loadEventComments(eventId.toLong())
                        eventViewModel.loadDetailsEvent(eventId = eventId.toLong())
                    }
                    eventViewModel.loadUserEventsByIds()
                }
                NOTIFICATION_EVENT_REFRESH_VALUE -> {
                    var eventId = intent.getStringExtra(EVENT_ID_INTENT)
                    eventViewModel.loadEvents()
                    eventViewModel.loadEventsByLocation()
                    eventViewModel.loadUserEventsByIds()
                    if (eventId != "" && eventViewModel.detailsEventId != null && eventViewModel.detailsEventId == eventId.toLong()) {
                        eventViewModel.loadEventComments(eventId.toLong())
                        eventViewModel.loadDetailsEvent(eventId = eventId.toLong())
                    }
                }
            }
        }
    }
}
