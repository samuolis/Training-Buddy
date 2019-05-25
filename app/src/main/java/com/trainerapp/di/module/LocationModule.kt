package com.trainerapp.di.module

import android.app.Activity
import com.trainerapp.service.LocationService
import com.trainerapp.service.LocationServiceImpl
import dagger.Module
import dagger.Provides

@Module
class LocationModule {

    @Provides
    fun provideLocationService(
            activity: Activity
    ): LocationService {
        return LocationServiceImpl(
                activity = activity
        )
    }
}
