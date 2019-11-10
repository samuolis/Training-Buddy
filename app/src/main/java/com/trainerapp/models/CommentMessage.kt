package com.trainerapp.models

data class CommentMessage(
        var messageText: String?,
        var userId: String?,
        var eventId: Long?,
        var messageTime: Long?,
        var messageUserName: String?
)
