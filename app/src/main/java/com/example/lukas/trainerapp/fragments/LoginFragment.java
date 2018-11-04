package com.example.lukas.trainerapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lukas.trainerapp.LoginActivity;
import com.example.lukas.trainerapp.MainActivity;
import com.example.lukas.trainerapp.R;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;
import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginFragment extends Fragment {

    private static final int APP_REQUEST_CODE = 10;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private static final String EMAIL = "email";

    @BindView(R.id.login_email)
    ConstraintLayout loginViaEmail;
    @BindView(R.id.login_phone) ConstraintLayout loginViaPhone;
    @BindView(R.id.facebook_login_button)
    LoginButton facebookLoginButton;
    @BindView(R.id.login_progress)
    ProgressBar mProgressView;
    @BindView(R.id.progress_bar_background) View mProgressBarBackground;
    private CallbackManager callbackManager;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        launchRegisterFragment();
                    }

                    @Override
                    public void onCancel() {
                        mProgressBarBackground.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        mProgressBarBackground.setVisibility(View.GONE);
                        String toastMessage = exception.getMessage();
                        Toast.makeText(getActivity(),toastMessage, Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this,rootView);
        AppDatabase db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "user")
                .fallbackToDestructiveMigration()
                .build();
        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        com.facebook.AccessToken fbToken = com.facebook.AccessToken.getCurrentAccessToken();
        if (accessToken != null || fbToken !=null){
            launchMainActivity();
        }


        loginViaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                onEmailLogin();
            }
        });

        loginViaPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                onPhoneLogin();
            }
        });

        facebookLoginButton.setFragment(this);
        facebookLoginButton.setReadPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBarBackground.setVisibility(View.VISIBLE);
            }
        });


        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE){
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null){
                String toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
            }else if (loginResult.getAccessToken() != null){
                launchRegisterFragment();
            }
        }
        if (resultCode == 0) {
            hideProgressBar();
        }
    }

    private void launchMainActivity(){
        Intent myIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(myIntent);
    }

    private void launchRegisterFragment(){
        ((LoginActivity)getActivity()).GoToRegisterFragment();
    }

    public void onLogin(final LoginType loginType){
        final Intent intent = new Intent(getActivity(), AccountKitActivity.class);

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
