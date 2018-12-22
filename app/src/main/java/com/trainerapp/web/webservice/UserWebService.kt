package com.trainerapp.web.webservice

import com.trainerapp.db.entity.User

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserWebService {

    @POST("/user/")
    fun postUser(@Body body: User): Call<User>

    @GET("/user/{userId}")
    fun getExistantUser(@Path("userId") userId: String?): Call<User>

    @POST("/users")
    fun getUserByIds(@Body userIds: List<String>?): Call<List<User>>
}
