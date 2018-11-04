package com.example.lukas.trainerapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.Window;


import com.example.lukas.trainerapp.fragments.LoginFragment;
import com.example.lukas.trainerapp.fragments.RegisterFragment;
import com.facebook.CallbackManager;

import androidx.fragment.app.FragmentManager;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private CallbackManager callbackManager;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_login);
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Begin the transaction
        fragmentManager.beginTransaction()
                .add(R.id.fragment_frame, loginFragment)
                .commit();
        // Replace the contents of the container with the new fragment
        //ft.replace(R.id.fragment_frame, new LoginFragment());
    }

    public void GoToRegisterFragment(){
        RegisterFragment registerFragment = new RegisterFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        actionBar.show();
        actionBar.setTitle("Fill registration");
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Begin the transaction
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_frame, registerFragment)
                .addToBackStack(null)
                .commit();
    }

    public void GoToMainActivity(){
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                actionBar.hide();
                getSupportFragmentManager().popBackStack();
                return true;
        }

        return false;
    }
}

