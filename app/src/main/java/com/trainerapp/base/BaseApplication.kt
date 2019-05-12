package com.trainerapp.base

import android.app.Application
import com.trainerapp.di.component.ApplicationComponent
import com.trainerapp.di.component.DaggerApplicationComponent
import com.trainerapp.di.module.ApplicationModule

class BaseApplication : Application() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
    }

    fun setup() {
        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this)).build()
        component.inject(this)
    }

    fun getApplicationComponent(): ApplicationComponent {
        return component
    }
}