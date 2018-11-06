package com.example.lukas.trainerapp.db.viewmodel;

import android.app.Application;
import android.util.Log;

import com.example.lukas.trainerapp.UserDataSource;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.model.Authorization;
import com.example.lukas.trainerapp.server.service.UserWebService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserViewModel extends AndroidViewModel {

    //private final UserDataSource mDataSource;

    private static final String TAG = UserViewModel.class.getSimpleName();

    private LiveData<User> mUser;

    private Authorization mAuthorization;

    private static final String BASE_URL = "https://trainingbuddy-221215.appspot.com/";


    public UserViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.i(TAG, "getting user in viewmodel");
        mUser = database.userDao().getUser();
    }

    /**
     * Get the user name of the user.
     *
     * @return a {@link Flowable} that will emit every time the user name has been updated.
     */
    public LiveData<User> getUser() {
        return mUser;
    }

    public Authorization getmAuthorization() {
        return mAuthorization;
    }

    public void setmAuthorization(Authorization mAuthorization) {
        this.mAuthorization = mAuthorization;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }
}
