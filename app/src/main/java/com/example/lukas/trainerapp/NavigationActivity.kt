package com.example.lukas.trainerapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.lukas.trainerapp.db.AppDatabase
import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.db.viewmodel.UserViewModel
import com.facebook.accountkit.AccountKit
import com.facebook.login.LoginManager
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        var userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        userViewModel.user.observe(this, Observer { user: User? ->
            supportActionBar?.title = user?.fullName
        })
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.action_settings -> {
            //do something
            true
        }
        R.id.action_logout -> {
            val mDb = AppDatabase.getInstance(applicationContext);
            AccountKit.logOut()
            LoginManager.getInstance().logOut()
            AppExecutors.getInstance().diskIO().execute {
                mDb.userDao().deleteAllUsers()
                val myIntent = Intent(this, LoginActivity::class.java)
                startActivity(myIntent)
                finish()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }


    }

    override fun onBackPressed() {
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
