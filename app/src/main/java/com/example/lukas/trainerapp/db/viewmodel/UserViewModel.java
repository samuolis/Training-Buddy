package com.example.lukas.trainerapp.db.viewmodel;

import android.app.Application;
import android.util.Log;

import com.example.lukas.trainerapp.AppExecutors;
import com.example.lukas.trainerapp.UserDataSource;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.model.Authorization;
import com.example.lukas.trainerapp.model.UserData;
import com.example.lukas.trainerapp.server.service.UserWebService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.RoomDatabase;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserViewModel extends AndroidViewModel {


    private static final String TAG = UserViewModel.class.getSimpleName();

    private LiveData<User> mUser;

    private MutableLiveData<UserData> mUserData;

    private AppDatabase database;

    private static final String BASE_URL = "https://training-222106.appspot.com/";


    public UserViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
        Log.i(TAG, "getting user in viewmodel");
        mUser = database.userDao().getUser();
    }

    public void deleteAllUser(){

    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public UserData getmUserData() {
        return mUserData.getValue();
    }

    public void setmUserData(UserData mUserData) {
        this.mUserData.setValue(mUserData);
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public void init() {
        mUserData = new MutableLiveData<UserData>();
    }
}
