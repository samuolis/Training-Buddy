package com.example.lukas.trainerapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lukas.trainerapp.AppExecutors;
import com.example.lukas.trainerapp.ui.LoginActivity;
import com.example.lukas.trainerapp.R;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.ui.viewmodel.UserViewModel;
import com.example.lukas.trainerapp.model.UserData;
import com.example.lukas.trainerapp.webService.UserWebService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.facebook.accountkit.ui.SkinManager.Skin.CONTEMPORARY;


public class LoginFragment extends Fragment {

    private static final int APP_REQUEST_CODE = 10;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private static final String EMAIL = "email";

    private static final String TAG = LoginFragment.class.getSimpleName();

    @BindView(R.id.login_email)
    ConstraintLayout loginViaEmail;
    @BindView(R.id.login_phone) ConstraintLayout loginViaPhone;
//    @BindView(R.id.facebook_login_button)
//    LoginButton facebookLoginButton;
    @BindView(R.id.login_progress)
    ProgressBar mProgressView;
    @BindView(R.id.progress_bar_background) View mProgressBarBackground;
    private CallbackManager callbackManager;

    private UserViewModel userViewModel;
    private AppDatabase mDb;
    private UserWebService userWebService;

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
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mDb = AppDatabase.getInstance(getContext());
        AppExecutors.getInstance().diskIO().execute(() -> {
            User user = mDb.userDao().getSimpleUser();
            if (user != null){
                ((LoginActivity)getActivity()).GoToNavigationActivity();
            }
        });
        ButterKnife.bind(this,rootView);

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

//        facebookLoginButton.setFragment(this);
//        facebookLoginButton.setReadPermissions(Arrays.asList(EMAIL));
//        // If you are using in a fragment, call loginButton.setFragment(this);
//
//        facebookLoginButton.setOnClickListener(view ->
//                mProgressBarBackground.setVisibility(View.VISIBLE));


        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(
                    AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                hideProgressBar();
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Canceled";
                hideProgressBar();
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
            } else {
                String authCode = loginResult.getAuthorizationCode();
                executeLogin(authCode);
            }
        }else{
            hideProgressBar();
        }
    }

    private void launchRegisterFragment(){
        ((LoginActivity)getActivity()).GoToRegisterFragment();
    }

    public void onLogin(final LoginType loginType){
        final Intent intent = new Intent(getActivity(), AccountKitActivity.class);

        //configure login type and resource type
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder;
        UIManager uiManager;
        uiManager = new SkinManager(
                CONTEMPORARY, getActivity().getResources().getColor(R.color.colorPrimary));

        configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.CODE);
        configurationBuilder.setUIManager(uiManager);
        final AccountKitConfiguration configuration = configurationBuilder.build();
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        startActivityForResult(intent,APP_REQUEST_CODE);

    }

    public void executeLogin(String authCode) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(userViewModel.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        userWebService = retrofit.create(UserWebService.class);
        String userId;
        userWebService.getUser(authCode).enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_id_preferences), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.user_id_key), Long.toString(response.body().getId()));
                        editor.commit();
                        userViewModel.setmUserData(response.body());
                        getUser(Long.toString(userViewModel.getmUserData().getId()));
                    } else {
                        hideProgressBar();
                        return;
                    }
                } else{
                    hideProgressBar();
                    Log.i(TAG, "Error code : " + response.code());
                    Log.i(TAG, "body : " +response.message());
                    Toast.makeText(getActivity(),response.message(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                hideProgressBar();
                Toast.makeText(getActivity(),t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getUser(String userId){
        userWebService.getExistantUser(userId)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if(response.isSuccessful()) {
                            AppExecutors.getInstance().diskIO().execute(() -> {
                                mDb.userDao().insertUser(response.body());
                                ((LoginActivity)getActivity()).GoToNavigationActivity();
                            });
                        } else {
                            ((LoginActivity)getActivity()).GoToRegisterFragment();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        ((LoginActivity)getActivity()).GoToRegisterFragment();
                    }
                });
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
