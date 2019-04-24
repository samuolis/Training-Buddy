package com.trainerapp

import com.trainerapp.di.component.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class BaseApplication : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication>? {
        val component = DaggerApplicationComponent.builder().application(this).build()
        component.inject(this)

        return null
    }
}