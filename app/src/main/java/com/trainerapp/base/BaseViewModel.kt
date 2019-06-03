package com.trainerapp.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trainerapp.extension.readOnly
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    private val viewModelJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    private val compositeDisposable = CompositeDisposable()

    protected val _error = MutableLiveData<Throwable>()
    val error = _error.readOnly()

    override fun onCleared() {
        compositeDisposable.clear()
        viewModelJob.cancel()
        super.onCleared()
    }

    fun Disposable.bind() {
        compositeDisposable.add(this)
    }
}
