package com.trainerapp.di.module

import com.trainerapp.base.BaseActivity
import com.trainerapp.manager.LoadingManager
import com.trainerapp.navigation.NavigationController
import com.trainerapp.navigation.NavigationControllerImpl
import dagger.Module
import dagger.Provides

@Module
class NavigationModule {

    @Provides
    fun providesNavigationController(
            activity: BaseActivity,
            loadingManager: LoadingManager
    ): NavigationController {
        return NavigationControllerImpl(activity.supportFragmentManager, loadingManager)
    }
}
