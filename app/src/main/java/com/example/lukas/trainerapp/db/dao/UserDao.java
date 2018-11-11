package com.example.lukas.trainerapp.db.dao;

import com.example.lukas.trainerapp.db.entity.User;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Flowable;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User")
    List<User> getAll();

    @Query("SELECT * FROM User WHERE userId IN (:userIds)")
    List<User> loadAllByIds(long[] userIds);

    @Query("SELECT * FROM User WHERE fullName LIKE :fullName LIMIT 1")
    User findByFullName(String fullName);

    @Query("SELECT * FROM User ORDER BY createdAt DESC LIMIT 1")
    LiveData<User> getUser();

    @Query("SELECT * FROM User ORDER BY createdAt DESC LIMIT 1")
    User getSimpleUser();

    @Insert
    void insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User login);

    @Query("DELETE FROM User")
    void deleteAllUsers();

    @Delete
    void delete(User user);
}
