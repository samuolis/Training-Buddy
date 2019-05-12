package com.trainerapp.models

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

class User {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @SerializedName("userId")
    @ColumnInfo(name = "userId")
    var userId: String? = null

    @SerializedName("fullName")
    @ColumnInfo(name = "fullName")
    var fullName: String? = null

    @SerializedName("email")
    @ColumnInfo(name = "email")
    var email: String? = null

    @SerializedName("createdOn")
    @ColumnInfo(name = "createdAt")
    var createdAt: Date? = null

    @SerializedName("profilePictureIndex")
    @ColumnInfo(name = "profilePictureIndex")
    var profilePictureIndex: Int? = null

    @Ignore
    @SerializedName("signedEventsList")
    var signedEventsList: List<Long>? = null

    @Ignore
    @SerializedName("userFcmToken")
    var userFcmToken: String? = null

    constructor(id: Long, userId: String?, fullName: String?, email: String?,
                createdAt: Date?) {
        this.id = id
        this.fullName = fullName
        this.email = email
        this.userId = userId
        this.createdAt = createdAt
    }

    @Ignore
    constructor(userId: String?, fullName: String?, email: String?,
                createdAt: Date?, profilePictureIndex: Int?, signedEventsList: List<Long>?, userFcmToken: String?) {
        this.fullName = fullName
        this.email = email
        this.userId = userId
        this.createdAt = createdAt
        this.profilePictureIndex = profilePictureIndex
        this.signedEventsList = signedEventsList
        this.userFcmToken = userFcmToken

    }
}
