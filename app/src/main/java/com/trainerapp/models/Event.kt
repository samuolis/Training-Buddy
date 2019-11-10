package com.trainerapp.models

import java.util.*

data class Event(
        val eventId: Long?,
        val userId: String?,
        val eventName: String?,
        val eventDescription: String?,
        val eventLocationName: String?,
        val eventLocationLatitude: Double?,
        val eventLocationLongitude: Double?,
        val eventLocationCountryCode: String?,
        val eventPlayers: Int?,
        val eventDate: Date?,
        var eventDistance: Float?,
        val eventSignedPlayers: List<User>?,
        val eventComments: List<CommentMessage>?
)
