package com.trainerapp.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.trainerapp.di.ViewModelFactory
import com.trainerapp.di.ViewModelKey
import com.trainerapp.ui.viewmodel.EventViewModel
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
}
