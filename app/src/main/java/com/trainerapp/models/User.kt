package com.trainerapp.models

import com.google.gson.annotations.SerializedName
import java.util.*

class User {

    var id: Long = 0

    @SerializedName("userId")
    var userId: String? = null

    @SerializedName("fullName")
    var fullName: String? = null


    @SerializedName("createdOn")
    var createdAt: Date? = null

    @SerializedName("profilePictureIndex")
    var profilePictureIndex: Int? = null

    @SerializedName("signedEventsList")
    var signedEventsList: List<Long>? = null


    constructor(
            id: Long,
            userId: String?,
            fullName: String?,
            createdAt: Date?
    ) {
        this.id = id
        this.fullName = fullName
        this.userId = userId
        this.createdAt = createdAt
    }

    constructor(
            userId: String?,
            fullName: String?,
            createdAt: Date?,
            profilePictureIndex: Int?,
            signedEventsList: List<Long>?
    ) {
        this.fullName = fullName
        this.userId = userId
        this.createdAt = createdAt
        this.profilePictureIndex = profilePictureIndex
        this.signedEventsList = signedEventsList

    }
}
