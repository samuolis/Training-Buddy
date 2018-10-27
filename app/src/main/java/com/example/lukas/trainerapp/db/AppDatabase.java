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
    private static AppDatabase INSTANCE;
    private static final String DB_NAME_LOGIN = "login.db";


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME_LOGIN)
                            .allowMainThreadQueries() // SHOULD NOT BE USED IN PRODUCTION !!!
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Log.d("LoginDatabase", "populating with data...");
                                    new PopulateDbAsync(INSTANCE).execute();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public void clearDb() {
        if (INSTANCE != null) {
            new PopulateDbAsync(INSTANCE).execute();
        }
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final LoginDao loginDao;
        public PopulateDbAsync(AppDatabase instance) {
            loginDao = instance.loginDao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            loginDao.deleteAll();
            return null;
        }
    }
}
