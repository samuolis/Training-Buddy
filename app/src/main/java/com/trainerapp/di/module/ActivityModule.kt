package com.trainerapp.di.module

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.GsonBuilder
import com.trainerapp.R
import com.trainerapp.web.webservice.EventWebService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ActivityModule(private var activity: Activity) {

    val BASE_URL = "https://training-222106.appspot.com/"

    @Provides
    fun providesGoogleSignIn(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString((R.string.default_web_client_id)))
                .requestEmail()
                .build()
        // [END config_signin]

        return GoogleSignIn.getClient(activity, gso)

    }

    @Provides
    fun providesEventWebService(): EventWebService {
        val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        return retrofit.create(EventWebService::class.java)
    }
}
