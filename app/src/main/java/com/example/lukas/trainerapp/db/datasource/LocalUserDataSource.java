package com.example.lukas.trainerapp.db.datasource;

import com.example.lukas.trainerapp.UserDataSource;
import com.example.lukas.trainerapp.db.dao.UserDao;
import com.example.lukas.trainerapp.db.entity.User;

import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;

public class LocalUserDataSource implements UserDataSource {

    private final UserDao mUserDao;

    public LocalUserDataSource(UserDao userDao) {
        mUserDao = userDao;
    }

    @Override
    public LiveData<User> getUser() {
        return mUserDao.getUser();
    }

    @Override
    public void insertOrUpdateUser(User user) {
        mUserDao.insertUser(user);
    }

    @Override
    public void deleteAllUsers() {
        mUserDao.deleteAllUsers();
    }
}
