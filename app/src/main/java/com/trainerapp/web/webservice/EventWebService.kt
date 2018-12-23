package com.trainerapp.web.webservice

import com.trainerapp.db.entity.Event
import com.trainerapp.models.CommentMessage
import retrofit2.Call
import retrofit2.http.*

interface EventWebService {

    @POST("/event")
    fun createEvent(@Body event: Event,
                    @Header("authorization-code") authorizationCode: String?): Call<Event>

    @GET("/event/{userId}")
    fun getEventsByUserId(@Path("userId") userId: String?): Call<List<Event>>

    @GET("/event/{userId}/{radius}/{countryCode}/{latitude}/{longitude}")
    fun getEventsByLocation(@Path("userId") userId: String?,
                            @Path("radius") radius: String?,
                            @Path("countryCode") countryCode: String?,
                            @Path("latitude") latitude: Float?,
                            @Path("longitude") longitude: Float?): Call<List<Event>>

    @POST("/event/{userId}/{eventId}")
    fun signEvent(@Path("userId") userId: String?,
                  @Path("eventId") eventId: Long?,
                  @Header("authorization-code") authorizationCode: String?): Call<Void>

    @POST("/event/delete/{userId}/{eventId}")
    fun unsignEvent(@Path("userId") userId: String?,
                    @Path("eventId") eventId: Long?,
                    @Header("authorization-code") authorizationCode: String?): Call<Void>

    @POST("/events")
    fun getEventByIds(@Body eventsIds: List<Long>?): Call<List<Event>>

    @DELETE("/event/delete/{eventId}")
    fun deleteEventById(@Path("eventId") eventId: Long?,
                        @Header("authorization-code") authorizationCode: String?): Call<Void>

    @GET("/event/one/{eventId}")
    fun getEventById(@Path("eventId") eventId: Long?): Call<Event>

    @POST("/event/comment")
    fun createCommentMessage(@Body commentMessage: CommentMessage,
                             @Header("authorization-code") authorizationCode: String?): Call<CommentMessage>

    @POST("/event/comments")
    fun getEventCommentsByIds(@Body eventsIds: List<Long>?): Call<List<CommentMessage>>

}