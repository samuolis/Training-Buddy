package com.trainerapp.di.module

import android.app.Application
import com.trainerapp.base.BaseApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val baseApplication: BaseApplication) {

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return baseApplication
    }

}