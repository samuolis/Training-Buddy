package com.example.lukas.trainerapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.AppExecutors
import com.example.lukas.trainerapp.R
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.example.lukas.trainerapp.ui.fragments.*
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    var profileFragment = ProfileFragment()
    var homeFragment = HomeFragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.navigation_frame, homeFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction()
                        .remove(profileFragment)
                        .remove(homeFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
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
    lateinit var userViewModel : UserViewModel
    lateinit var logoutIntent : Intent
    var fragmentAdded : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        supportActionBar?.title = getString(R.string.app_name)
        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp()
        supportFragmentManager.beginTransaction()
                .replace(R.id.navigation_frame, homeFragment)
                .commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onBackStackChanged() {
        fragmentAdded = supportFragmentManager.backStackEntryCount > 0
        if (!fragmentAdded){
            supportActionBar!!.title = getString(R.string.app_name)
        }
        shouldDisplayHomeUp()
        invalidateOptionsMenu()
    }

    fun shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        supportActionBar!!.setDisplayHomeAsUpEnabled(fragmentAdded)
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.action_logout -> {
            var database = AppDatabase.getInstance(this.application)
            AppExecutors.getInstance().diskIO().execute {
                database.userDao().deleteAllUsers()
                Logout()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }


    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.count() > 1){
            supportFragmentManager.popBackStack()
        }
        else {
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

    public fun Logout(){
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        logoutIntent = Intent(this@NavigationActivity, LoginActivity::class.java)
        startActivity(logoutIntent)
        finish()
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

    fun backOnStack(){
        supportFragmentManager.popBackStack()
    }
}
