package com.example.lukas.trainerapp.db.entity;

import com.example.lukas.trainerapp.db.DateConverter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "user")
@TypeConverters(DateConverter.class)
public class User {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @SerializedName("userId")
    @ColumnInfo(name = "userId")
    private String userId;

    @SerializedName("fullName")
    @ColumnInfo(name = "fullName")
    private String fullName;

    @SerializedName("email")
    @ColumnInfo(name = "email")
    private String email;

    @SerializedName("phoneNumber")
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;

    @SerializedName("createdOn")
    @ColumnInfo(name = "createdAt")
    private Date createdAt;

    @Ignore
    public User(String userId, String fullName, String email, String phoneNumber, Date createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public User(long id, String fullName, String email, String phoneNumber, Date createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
