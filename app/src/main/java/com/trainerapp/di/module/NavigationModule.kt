package com.trainerapp.di.module

import com.trainerapp.base.BaseActivity
import com.trainerapp.navigation.NavigationController
import com.trainerapp.navigation.NavigationControllerImpl
import dagger.Module
import dagger.Provides

@Module
class NavigationModule {

    @Provides
    fun providesNavigationController(activity: BaseActivity): NavigationController {
        return NavigationControllerImpl(activity.supportFragmentManager)
    }
}
