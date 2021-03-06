package com.trainerapp.di.component

import com.trainerapp.base.BaseActivity
import com.trainerapp.base.BaseFragment
import com.trainerapp.di.PerActivityScope
import com.trainerapp.di.module.ActivityModule
import com.trainerapp.di.module.LocationModule
import com.trainerapp.di.module.NavigationModule
import com.trainerapp.di.module.ViewModelModule
import com.trainerapp.feature.add_event.AddEventDialogFragment
import com.trainerapp.ui.LoginActivity
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.ui.fragments.*
import dagger.Component

@PerActivityScope
@Component(modules = [
    ActivityModule::class,
    ViewModelModule::class,
    LocationModule::class,
    NavigationModule::class
])
interface ActivityComponent {

    fun inject(mainActivity: NavigationActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(baseActivity: BaseActivity)

    fun inject(eventDetailsDialogFragment: EventDetailsDialogFragment)

    fun inject(addEventDialogFragment: AddEventDialogFragment)

    fun inject(eventCommentsDialogFragment: EventCommentsDialogFragment)

    fun inject(homeFragment: HomeFragment)

    fun inject(dashboardFragment: DashboardFragment)

    fun inject(profileFragment: ProfileFragment)

    fun inject(loginFragment: LoginFragment)

    fun inject(accountEditDialogFragment: AccountEditDialogFragment)

    fun inject(archivedEventsDialogFragment: ArchivedEventsDialogFragment)

    fun inject(searchFragment: SearchFragment)

    fun inject(eventSignedUsersListDialogFragment: EventSignedUsersListDialogFragment)

    fun inject(baseFragment: BaseFragment)
}
