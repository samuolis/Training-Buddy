package com.example.lukas.trainerapp.db.dao;

import com.example.lukas.trainerapp.db.entity.User;

import java.util.List;

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

    @Query("SELECT * FROM User WHERE id IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM User WHERE full_name LIKE :fullName LIMIT 1")
    User findByFullName(String fullName);

    @Query("SELECT * FROM User LIMIT 1")
    Flowable<User> getUserById();

    @Insert
    void insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User login);

    @Query("DELETE FROM User")
    void deleteAllUsers();

    @Delete
    void delete(User user);
}
