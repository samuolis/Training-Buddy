package com.trainerapp.di.component

import com.trainerapp.base.BaseApplication
import com.trainerapp.di.module.ApplicationModule
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(application: BaseApplication)
}