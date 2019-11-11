package com.trainerapp.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerFragmentScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerApplicationScope
