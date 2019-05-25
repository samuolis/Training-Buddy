package com.trainerapp.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

fun <T> LiveData<T>.nonNullObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(
        viewModelFactory: ViewModelProvider.Factory
): T {
    return ViewModelProviders.of(this, viewModelFactory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.getViewModel(
        viewModelFactory: ViewModelProvider.Factory
): T {
    return ViewModelProviders.of(this.activity!!, viewModelFactory)[T::class.java]
}

fun <T : Any> MutableLiveData<T>.readOnly(): LiveData<T> {
    return this
}
