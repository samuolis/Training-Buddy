package com.example.lukas.trainerapp.model;

import com.google.gson.annotations.SerializedName;

public class UserData {

    @SerializedName("id")
    private long id;

    @SerializedName("email")
    private Email email;

    @SerializedName("phone")
    private Phone phone;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }
}
