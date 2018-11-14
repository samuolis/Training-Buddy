package com.example.lukas.trainerapp.server.service

import com.example.lukas.trainerapp.db.entity.User
import com.example.lukas.trainerapp.model.Authorization
import com.example.lukas.trainerapp.model.UserData

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserWebService {
    /**
     * @GET declares an HTTP GET request
     * @Path("user") annotation on the userId parameter marks it as a
     * replacement for the {user} placeholder in the @GET path
     */
    @GET("/authentication")
    fun getUser(@Header("authorization-code") authorizationCode: String): Call<UserData>

    @POST("/user/")
    fun postUser(@Body body: User): Call<User>

    @GET("/user/{userId}")
    fun getExistantUser(@Path("userId") userId: String): Call<User>
}
