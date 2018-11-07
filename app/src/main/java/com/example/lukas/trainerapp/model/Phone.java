package com.example.lukas.trainerapp.model;

import com.google.gson.annotations.SerializedName;

public class Phone {

    public String number;

    public String prefix;

    @SerializedName("national_name")
    public String nationalNumber;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    public void setNationalNumber(String nationalNumber) {
        this.nationalNumber = nationalNumber;
    }
}
