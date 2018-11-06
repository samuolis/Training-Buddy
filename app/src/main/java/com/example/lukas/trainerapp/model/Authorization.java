package com.example.lukas.trainerapp.model;

import com.google.gson.annotations.SerializedName;

public class Authorization {

    @SerializedName("id")
    public long id;

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_refresh_interval_sec")
    public long tokenRefreshTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getTokenRefreshTime() {
        return tokenRefreshTime;
    }

    public void setTokenRefreshTime(long tokenRefreshTime) {
        this.tokenRefreshTime = tokenRefreshTime;
    }
}
