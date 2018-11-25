package com.example.lukas.trainerapp.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lukas.trainerapp.AppExecutors;
import com.example.lukas.trainerapp.ui.LoginActivity;
import com.example.lukas.trainerapp.R;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel;
import com.example.lukas.trainerapp.model.UserData;
import com.example.lukas.trainerapp.web.webservice.UserWebService;
import com.facebook.Profile;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.READ_CONTACTS;
import static androidx.core.content.PermissionChecker.checkSelfPermission;


public class RegisterFragment extends Fragment{

    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.full_name)
    EditText mFullNameEditText;
    @BindView(R.id.phone_number_edittext)
    EditText mPhoneNumberEditText;
    @BindView(R.id.register_button)
    Button mRegisterButton;
    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form) View mLoginFormView;

    private AppDatabase mDb;
    private UserViewModel userViewModel;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_register, container, false);
        ButterKnife.bind(this,rootView);
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mRegisterButton.setOnClickListener((View v) -> {
            attemptRegister();
        });

        mDb = AppDatabase.getInstance(getContext());

        populateAutoComplete();
        fillInformation();

        return rootView;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
    }

    private void fillInformation(){

        if(com.facebook.AccessToken.getCurrentAccessToken() != null)
        {
            Profile currentProfile = Profile.getCurrentProfile();
            if (currentProfile != null) {
                mFullNameEditText.setText(currentProfile.getFirstName() + " " + currentProfile.getLastName());
            }
        }
        else {
            UserData userData = userViewModel.getmUserData();
            if (userData.getPhone() != null) {
                String phoneNumber = userViewModel.getmUserData().getPhone().getNumber();
                mPhoneNumberEditText.setText(phoneNumber);
            }
            if (userData.getEmail() != null) {
                String emailString = userViewModel.getmUserData().getEmail().getAddress();
                mEmailView.setText(emailString);
            }
        }
    }




    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(getContext(), READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void attemptRegister() {

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String phoneNumber = mPhoneNumberEditText.getText().toString();
        String fullName = mFullNameEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberEditText.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            String accessToken = getAccessToken();
            if (accessToken == null)
            {
                accessToken = "";
            }
            Date currentTime = Calendar.getInstance().getTime();
            Long userId = userViewModel.getmUserData().getId();

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("LoginFragment", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        final User user = new User(userId.toString(), fullName, email, phoneNumber,
                                currentTime, null, null, token);

                        Gson gson = new GsonBuilder()
                                .setLenient()
                                .create();

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(userViewModel.getBaseUrl())
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build();

                        UserWebService userWebService = retrofit.create(UserWebService.class);

                        userWebService.postUser(user).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                AppExecutors.getInstance().diskIO().execute(() -> {
                                    try {
                                        mDb.userDao().insertUser(response.body());
                                        ((LoginActivity)getActivity()).GoToNavigationActivity();
                                    } catch (Exception e){
                                        Toast.makeText(getActivity(), "failed to store data", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                Toast.makeText(getActivity(),t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    });

        }
    }

    private String getAccessToken(){
        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        com.facebook.AccessToken fbToken = com.facebook.AccessToken.getCurrentAccessToken();
        if (accessToken != null){
            return accessToken.toString();
        } else if (fbToken != null){
            return fbToken.toString();
        } else {
            return null;
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
