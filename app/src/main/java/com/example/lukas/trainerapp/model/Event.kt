package com.example.lukas.trainerapp.model

import androidx.room.TypeConverters
import com.example.lukas.trainerapp.db.converter.DateConverter

import java.util.Date

@TypeConverters(DateConverter::class)
data class Event(val eventId: Long?, val userId: String?, val eventName: String?,
                 val eventDescription: String?, val eventLocationName: String?,
                 val eventLocationLatitude: Double? , val eventLocationLongitude: Double?,
                 val eventPlayers: Int?, val eventDate: Date?)