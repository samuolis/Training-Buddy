package com.trainerapp.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.trainerapp.di.ViewModelFactory
import com.trainerapp.di.ViewModelKey
import com.trainerapp.feature.add_event.AddEventViewModel
import com.trainerapp.ui.viewmodel.EventDetailsViewModel
import com.trainerapp.ui.viewmodel.EventViewModel
import com.trainerapp.ui.viewmodel.LoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(EventViewModel::class)
    internal abstract fun eventViewModel(viewModel: EventViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EventDetailsViewModel::class)
    internal abstract fun eventDetailsViewModel(viewModel: EventDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddEventViewModel::class)
    internal abstract fun addEventViewModel(viewModel: AddEventViewModel): ViewModel
}
