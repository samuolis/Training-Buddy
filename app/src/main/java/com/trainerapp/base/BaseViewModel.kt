package com.trainerapp.base

import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.trainerapp.extension.readOnly
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    private val viewModelJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    private val compositeDisposable = CompositeDisposable()

    protected val _error = LiveEvent<Throwable>()
    val error = _error.readOnly()

    protected val _loadingStatus = LiveEvent<Boolean>()
    val loadingStatus = _loadingStatus.readOnly()

    override fun onCleared() {
        compositeDisposable.clear()
        viewModelJob.cancel()
        super.onCleared()
    }

    fun CoroutineScope.launchWithProgress(block: suspend CoroutineScope.() -> Unit) {
        launch {
            try {
                _loadingStatus.postValue(true)
                block()
            } finally {
                _loadingStatus.postValue(false)
            }
        }
    }

    fun Disposable.bind(): Disposable {
        compositeDisposable.add(this)
        return this
    }
}
