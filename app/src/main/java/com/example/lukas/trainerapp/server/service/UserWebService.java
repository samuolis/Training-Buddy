package com.example.lukas.trainerapp.server.service;

import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.model.Authorization;
import com.example.lukas.trainerapp.model.UserData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserWebService {
    /**
     * @GET declares an HTTP GET request
     * @Path("user") annotation on the userId parameter marks it as a
     * replacement for the {user} placeholder in the @GET path
     */
    @GET("/users")
    Call<UserData> getUser(@Header("authorization-code") String authorizationCode);
}
