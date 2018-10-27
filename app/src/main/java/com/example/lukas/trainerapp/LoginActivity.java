package com.example.lukas.trainerapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int APP_REQUEST_CODE = 10;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private static final String EMAIL = "email";

    @BindView(R.id.login_email) ConstraintLayout loginViaEmail;
    @BindView(R.id.login_phone) ConstraintLayout loginViaPhone;
    @BindView(R.id.facebook_login_button) LoginButton facebookLoginButton;
    @BindView(R.id.login_progress) ProgressBar mProgressView;
    @BindView(R.id.progress_bar_background) View mProgressBarBackground;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this); // bind butterknife after
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        com.facebook.AccessToken fbToken = com.facebook.AccessToken.getCurrentAccessToken();
        Toast.makeText(this, "fb token "+fbToken, Toast.LENGTH_LONG).show();
        if (accessToken != null || fbToken !=null){
            launchMainActivity();
        }


        loginViaEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                onEmailLogin();
            }
        });

        loginViaPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                onPhoneLogin();
            }
        });

        facebookLoginButton.setReadPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        facebookLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBarBackground.setVisibility(View.VISIBLE);
            }
        });

        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                launchMainActivity();
            }

            @Override
            public void onCancel() {
                mProgressBarBackground.setVisibility(View.GONE);
            }

            @Override
            public void onError(FacebookException error) {
                mProgressBarBackground.setVisibility(View.GONE);
                String toastMessage = error.getMessage();
                Toast.makeText(LoginActivity.this,toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchMainActivity(){
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE){
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null){
                String toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            }else if (loginResult.getAccessToken() != null){
                launchMainActivity();
            }
        }
        if (resultCode == 0) {
            hideProgressBar();
        }
    }

    public void onLogin(final LoginType loginType){
        final Intent intent = new Intent(this, AccountKitActivity.class);

        //configure login type and resource type
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.TOKEN);
        final AccountKitConfiguration configuration = configurationBuilder.build();
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        startActivityForResult(intent,APP_REQUEST_CODE);

    }

    public void onPhoneLogin(){
        onLogin(LoginType.PHONE);
    }

    public void onEmailLogin(){
        onLogin(LoginType.EMAIL);
    }


    private void showProgressBar()
    {
        mProgressView.setVisibility(View.VISIBLE);
        mProgressBarBackground.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar()
    {
        mProgressView.setVisibility(View.GONE);
        mProgressBarBackground.setVisibility(View.GONE);
    }

}

