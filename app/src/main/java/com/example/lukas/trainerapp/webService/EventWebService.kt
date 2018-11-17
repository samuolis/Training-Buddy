package com.example.lukas.trainerapp.webService

import com.example.lukas.trainerapp.model.Event
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface EventWebService {

    @POST("/event")
    fun createEvent(@Body event: Event): Call<Event>

}