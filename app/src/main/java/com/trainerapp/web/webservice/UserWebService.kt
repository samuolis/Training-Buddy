package com.trainerapp.web.webservice

import com.trainerapp.models.User
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserWebService {

    @POST("/user/")
    fun postUser(@Body body: User): Single<User>

    @GET("/user/{userId}")
    fun getExistantUser(@Path("userId") userId: String?): Single<User>
}
