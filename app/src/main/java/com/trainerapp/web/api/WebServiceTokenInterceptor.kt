package com.trainerapp.web.api

import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class WebServiceTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val containsTokenHeader = chain.request().headers().names().contains(HTTP_HEADER_OAUTH)

        if (!containsTokenHeader) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    throw Exception("User is not logged in.")
                } else {
                    val task = user.getIdToken(true)
                    val tokenResult = await<GetTokenResult>(task)
                    val idToken = tokenResult.token

                    if (idToken == null) {
                        throw Exception("idToken is null")
                    } else {
                        val modifiedRequest = request.newBuilder()
                                .addHeader(HTTP_HEADER_OAUTH, idToken)
                                .build()
                        return chain.proceed(modifiedRequest)
                    }
                }
            } catch (e: Exception) {
                throw IOException(e.message)
            }
        } else {
            return chain.proceed(request)
        }
    }

    companion object {
        private const val HTTP_HEADER_OAUTH = "authorization-code"
    }
}
