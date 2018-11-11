package com.example.lukas.trainerapp.server.service;

import com.example.lukas.trainerapp.db.entity.User;
import com.example.lukas.trainerapp.model.Authorization;
import com.example.lukas.trainerapp.model.UserData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserWebService {
    /**
     * @GET declares an HTTP GET request
     * @Path("user") annotation on the userId parameter marks it as a
     * replacement for the {user} placeholder in the @GET path
     */
    @GET("/authentication")
    Call<UserData> getUser(@Header("authorization-code") String authorizationCode);

    @POST("/user/")
    Call<User> postUser(@Body User body);

    @GET("/user/{userId}")
    Call<User> getExistantUser(@Path("userId") String userId);
}
