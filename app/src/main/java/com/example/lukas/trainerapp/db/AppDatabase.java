package com.example.lukas.trainerapp.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.lukas.trainerapp.db.Dao.LoginDao;
import com.example.lukas.trainerapp.db.entity.LoginEntity;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {LoginEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LoginDao loginDao();

}
