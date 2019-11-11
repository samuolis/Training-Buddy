package com.trainerapp.di.module

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tbruyelle.rxpermissions2.RxPermissions
import com.trainerapp.R
import com.trainerapp.base.BaseActivity
import com.trainerapp.manager.LoadingManager
import com.trainerapp.manager.LoadingManagerImpl
import com.trainerapp.service.PermissionService
import com.trainerapp.service.PermissionServiceImpl
import com.trainerapp.web.api.WebServiceTokenInterceptor
import com.trainerapp.web.webservice.EventWebService
import com.trainerapp.web.webservice.UserWebService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ActivityModule(private var activity: BaseActivity) {

    private val BASE_URL = "https://training-222106.appspot.com/"

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(WebServiceTokenInterceptor())
                .build()
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()
    }

    @Provides
    fun provideRetrofit(
            gson: Gson,
            okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    @Provides
    fun provideLoadingManager(activity: BaseActivity): LoadingManager {
        return LoadingManagerImpl(activity)
    }

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
    fun providesEventWebService(
            retrofit: Retrofit
    ): EventWebService {
        return retrofit.create(EventWebService::class.java)
    }

    @Provides
    fun providesUserWebService(
            retrofit: Retrofit
    ): UserWebService {
        return retrofit.create(UserWebService::class.java)
    }

    @Provides
    fun providesActivity(): Activity {
        return activity
    }

    @Provides
    fun providesBaseActivity(): BaseActivity {
        return activity
    }

    @Provides
    fun providePermissionsService(activity: Activity): PermissionService {
        return PermissionServiceImpl(activity, RxPermissions(activity as FragmentActivity))
    }
}
