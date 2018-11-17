package com.example.lukas.trainerapp.db;

import android.content.Context;

import com.example.lukas.trainerapp.db.dao.UserDao;
import com.example.lukas.trainerapp.db.dao.UserEventsDao;
import com.example.lukas.trainerapp.db.entity.Event;
import com.example.lukas.trainerapp.db.entity.User;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Event.class}, version = 14, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract UserEventsDao userEventsDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "User.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
