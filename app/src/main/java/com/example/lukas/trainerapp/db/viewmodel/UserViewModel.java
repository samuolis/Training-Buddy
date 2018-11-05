package com.example.lukas.trainerapp.db.viewmodel;

import android.app.Application;
import android.util.Log;

import com.example.lukas.trainerapp.UserDataSource;
import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.entity.User;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;

public class UserViewModel extends AndroidViewModel {

    //private final UserDataSource mDataSource;

    private static final String TAG = UserViewModel.class.getSimpleName();

    private LiveData<User> mUser;

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

    /**
     * Update the user name.
     *
     * @param user the new user name
     * @return a {@link Completable} that completes when the user name is updated
     */
//    public Completable updateUserName(final User user) {
//        return Completable.fromAction(() -> {
//            // if there's no use, create a new user.
//            // if we already have a user, then, since the user object is immutable,
//            // create a new user, with the id of the previous user and the updated user name.
//            mUser = user;
//
//            mDataSource.insertOrUpdateUser(mUser);
//        });
//    }
}
