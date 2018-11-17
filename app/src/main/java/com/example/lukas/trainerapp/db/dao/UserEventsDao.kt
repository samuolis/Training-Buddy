package com.example.lukas.trainerapp.db.dao

import androidx.room.*
import com.example.lukas.trainerapp.db.entity.Event

@Dao
interface UserEventsDao {

    @get:Query("SELECT * FROM user_events")
    val all: List<Event>

    @Query("SELECT * FROM user_events WHERE userId IN (:userIds)")
    fun loadAllByIds(userIds: LongArray): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllEvents (userEvents: List<Event>)

    @Query("DELETE FROM user_events WHERE eventId IN (:eventId)")
    fun deleteEventById(eventId: Long)
}