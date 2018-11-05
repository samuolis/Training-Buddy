package com.example.lukas.trainerapp;

import com.example.lukas.trainerapp.db.entity.User;

import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;

public interface UserDataSource {

    /**
     * Gets the user from the data source.
     *
     * @return the user from the data source.
     */
    LiveData<User> getUser();

    /**
     * Inserts the user into the data source, or, if this is an existing user, updates it.
     *
     * @param user the user to be inserted or updated.
     */
    void insertOrUpdateUser(User user);

    /**
     * Deletes all users from the data source.
     */
    void deleteAllUsers();
}
