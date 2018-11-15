package com.example.lukas.trainerapp.db.entity

import android.net.Uri

import com.example.lukas.trainerapp.db.converter.DateConverter
import com.example.lukas.trainerapp.db.converter.UriConverter
import com.google.gson.annotations.SerializedName

import java.util.Date

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "user")
@TypeConverters(DateConverter::class, UriConverter::class)
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

    @SerializedName("phoneNumber")
    @ColumnInfo(name = "phoneNumber")
    var phoneNumber: String? = null

    @SerializedName("createdOn")
    @ColumnInfo(name = "createdAt")
    var createdAt: Date? = null

    @SerializedName("profilePictureIndex")
    @ColumnInfo(name = "profilePictureIndex")
    var profilePictureIndex: Int? = null

    @Ignore
    constructor(userId: String?, fullName: String?, email: String?, phoneNumber: String?,
                createdAt: Date?) {
        this.fullName = fullName
        this.email = email
        this.phoneNumber = phoneNumber
        this.userId = userId
        this.createdAt = createdAt
    }

    constructor(id: Long, userId: String?, fullName: String?, email: String?, phoneNumber: String?,
                createdAt: Date?, profilePictureIndex: Int?) {
        this.id = id
        this.fullName = fullName
        this.email = email
        this.userId = userId
        this.phoneNumber = phoneNumber
        this.createdAt = createdAt
        this.profilePictureIndex = profilePictureIndex
    }
}
