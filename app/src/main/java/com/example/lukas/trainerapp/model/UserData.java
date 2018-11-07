package com.example.lukas.trainerapp.model;

import com.google.gson.annotations.SerializedName;

public class UserData {

    @SerializedName("id")
    public long id;

    @SerializedName("email")
    public Email email;

    @SerializedName("phone")
    public Phone phone;

}
