package com.trainerapp.web.webservice

import com.trainerapp.models.CommentMessage
import com.trainerapp.models.Event
import io.reactivex.Single
import retrofit2.http.*

interface EventWebService {

    @POST("/event")
    fun createEvent(@Body event: Event): Single<Event>

    @GET("/event/{userId}")
    fun getEventsByUserId(@Path("userId") userId: String?): Single<List<Event>>

    @GET("/event/{userId}/{radius}/{countryCode}/{latitude}/{longitude}")
    fun getEventsByLocation(@Path("userId") userId: String?,
                            @Path("radius") radius: String?,
                            @Path("countryCode") countryCode: String?,
                            @Path("latitude") latitude: Float?,
                            @Path("longitude") longitude: Float?): Single<List<Event>>

    @POST("/event/{userId}/{eventId}")
    fun signEvent(@Path("userId") userId: String?,
                  @Path("eventId") eventId: Long?,
                  @Header("authorization-code") authorizationCode: String?): Single<Event>

    @POST("/event/delete/{userId}/{eventId}")
    fun unsignEvent(@Path("userId") userId: String?,
                    @Path("eventId") eventId: Long?,
                    @Header("authorization-code") authorizationCode: String?): Single<Event>

    @POST("/events/signed/{userId}")
    fun getUserSignedEvents(@Path("userId") userId: String?): Single<List<Event>>

    @DELETE("/event/delete/{eventId}")
    fun deleteEventById(@Path("eventId") eventId: Long?,
                        @Header("authorization-code") authorizationCode: String?): Single<Void>

    @GET("/event/one/{eventId}")
    fun getEventById(@Path("eventId") eventId: Long?): Single<Event>

    @POST("/event/comment")
    fun createCommentMessage(@Body commentMessage: CommentMessage,
                             @Header("authorization-code") authorizationCode: String?): Single<CommentMessage>

}
