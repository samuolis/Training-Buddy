package com.trainerapp.db.dao

import com.trainerapp.db.entity.User

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.facebook.internal.Mutable
import io.reactivex.Flowable

@Dao
interface UserDao {

    @get:Query("SELECT * FROM User")
    val all: List<User>

    @get:Query("SELECT * FROM User ORDER BY createdAt DESC LIMIT 1")
    val user: LiveData<User>

    @get:Query("SELECT * FROM User ORDER BY createdAt DESC LIMIT 1")
    val simpleUser: User

    @Query("SELECT * FROM User WHERE userId IN (:userIds)")
    fun loadAllByIds(userIds: LongArray): List<User>

    @Query("SELECT * FROM User WHERE fullName LIKE :fullName LIMIT 1")
    fun findByFullName(fullName: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(login: User): Long

    @Query("DELETE FROM User")
    fun deleteAllUsers()

    @Delete
    fun delete(user: User)
}
