package com.example.lukas.trainerapp.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lukas.trainerapp.db.converter.DateConverter
import com.example.lukas.trainerapp.db.converter.UriConverter

import java.util.Date

data class Event(@PrimaryKey(autoGenerate = true) val eventId: Long?, val userId: String?, val eventName: String?,
                 val eventDescription: String?, val eventLocationName: String?,
                 val eventLocationLatitude: Double?, val eventLocationLongitude: Double?,
                 val eventLocationCountryCode: String?, val eventPlayers: Int?, val eventDate: Date?,
                 val eventDistance: Float?, val eventSignedPlayers: List<String>?)