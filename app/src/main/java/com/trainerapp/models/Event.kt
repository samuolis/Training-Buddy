package com.trainerapp.models

import com.trainerapp.feature.add_event.EventLocation
import java.util.*

data class Event(
        val eventId: Long?,
        val userId: String?,
        val eventName: String?,
        val eventDescription: String?,
        val eventLocation: EventLocation?,
        val eventPlayers: Int?,
        val eventDate: Date?,
        var eventDistance: Float?,
        val eventSignedPlayers: List<User>?,
        val eventComments: List<CommentMessage>?
)
