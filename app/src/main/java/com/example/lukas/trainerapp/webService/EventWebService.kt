package com.example.lukas.trainerapp.webService

import com.example.lukas.trainerapp.db.entity.Event
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EventWebService {

    @POST("/event")
    fun createEvent(@Body event: Event): Call<Event>

    @GET("/event/{userId}")
    fun getEventsByUserId(@Path("userId") userId: String?): Call<List<Event>>

    @GET("/event/{userId}/{radius}/{countryCode}/{latitude}/{longitude}")
    fun getEventsByLocation(@Path("userId") userId: String?,
                            @Path("radius") radius: String?,
                            @Path("countryCode") countryCode: String?,
                            @Path("latitude") latitude: Float?,
                            @Path("longitude") longitude: Float?): Call<List<Event>>

}