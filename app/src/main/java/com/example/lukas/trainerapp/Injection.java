package com.example.lukas.trainerapp;

import android.content.Context;

import com.example.lukas.trainerapp.db.AppDatabase;
import com.example.lukas.trainerapp.db.datasource.LocalUserDataSource;
import com.example.lukas.trainerapp.db.viewmodel.ViewModelFactory;

public class Injection {

    public static UserDataSource provideUserDataSource(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        return new LocalUserDataSource(database.userDao());
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        UserDataSource dataSource = provideUserDataSource(context);
        return new ViewModelFactory(dataSource);
    }
}
