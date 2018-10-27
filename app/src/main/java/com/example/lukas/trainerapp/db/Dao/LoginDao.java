package com.example.lukas.trainerapp.db.Dao;

import com.example.lukas.trainerapp.db.entity.LoginEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LoginDao {

    @Query("SELECT * FROM login_entity")
    List<LoginEntity> getAll();

    @Query("SELECT * FROM login_entity WHERE id IN (:userIds)")
    List<LoginEntity> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM login_entity WHERE first_name LIKE :first AND "
            + "last_name LIKE :last LIMIT 1")
    LoginEntity findByName(String first, String last);

    @Insert
    void insertAll(LoginEntity... logins);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(LoginEntity login);

    @Query("DELETE FROM login_entity")
    void deleteAll();

    @Delete
    void delete(LoginEntity login);
}
