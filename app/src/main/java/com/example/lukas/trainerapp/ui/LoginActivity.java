package com.example.lukas.trainerapp.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.Window;


import com.example.lukas.trainerapp.R;
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel;
import com.example.lukas.trainerapp.ui.fragments.LoginFragment;
import com.example.lukas.trainerapp.ui.fragments.RegisterFragment;
import com.facebook.CallbackManager;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private CallbackManager callbackManager;
    private ActionBar actionBar;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        setContentView(R.layout.activity_login);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
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

    public void GoToNavigationActivity(){
        Intent myIntent = new Intent(LoginActivity.this, NavigationActivity.class);
        startActivity(myIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
        }

        return false;
    }
}

