package com.trainerapp.web.api

import com.google.firebase.auth.FirebaseAuth
import com.trainerapp.extension.toSingle
import okhttp3.Interceptor
import okhttp3.Response

class WebServiceTokenInterceptor : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val containsTokenHeader = chain.request().headers().names().contains(HTTP_HEADER_OAUTH)

        if (!containsTokenHeader) {
            val token = FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.getIdToken(true)
                    ?.toSingle()
                    ?.blockingGet()
                    ?.token ?: ""
            requestBuilder.header(HTTP_HEADER_OAUTH, token)
        }

        return chain.proceed(requestBuilder.build())
    }

    companion object {
        private const val HTTP_HEADER_OAUTH = "authorization-code"
    }
}
