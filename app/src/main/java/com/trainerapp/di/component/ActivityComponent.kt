package com.trainerapp.di.component

import com.trainerapp.base.BaseActivity
import com.trainerapp.di.module.ActivityModule
import com.trainerapp.ui.LoginActivity
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.fragments.AddEventDialogFragment
import com.trainerapp.ui.fragments.EventCommentsDialogFragment
import com.trainerapp.ui.fragments.EventDetailsDialogFragment
import dagger.Component

@Component(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun inject(mainActivity: NavigationActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(baseActivity: BaseActivity)

    fun inject(eventDetailsDialogFragment: EventDetailsDialogFragment)

    fun inject(addEventDialogFragment: AddEventDialogFragment)

    fun inject(eventCommentsDialogFragment: EventCommentsDialogFragment)

}
