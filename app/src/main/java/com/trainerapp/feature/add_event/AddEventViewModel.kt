package com.trainerapp.feature.add_event

import android.location.Address
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.readOnly
import com.trainerapp.models.Event
import com.trainerapp.navigation.NavigationController
import com.trainerapp.service.LocationService
import com.trainerapp.web.webservice.EventWebService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddEventViewModel @Inject constructor(
        private val locationService: LocationService,
        private val navigationController: NavigationController,
        private val eventWebService: EventWebService
) : BaseViewModel() {

    private val addressQueryChanged: Subject<String> = PublishSubject.create()
    private var currentAddressQueryChangedDisposable: Disposable? = null

    private val _addresses = MutableLiveData<List<Address>>()
    val addresses = _addresses.readOnly()

    fun initialize() {
        restartQueryChanged()
    }

    private fun restartQueryChanged() {
        currentAddressQueryChangedDisposable?.dispose()
        currentAddressQueryChangedDisposable = addressQueryChanged
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .flatMap { query ->
                    locationService.getAddressByText(query)
                            .subscribeOn(Schedulers.io())
                            .toObservable()
                            .materialize()
                            .filter { !it.isOnComplete }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            _addresses.value = it.value
                        },
                        onError = {
                            _error.postValue(it)
                        }
                ).bind()
    }

    fun onLocationTextChanged(charSequence: CharSequence) {
        addressQueryChanged.onNext(charSequence.toString())
    }

    fun createEvent(event: Event) {
        launchWithProgress {
            try {
                eventWebService.createEvent(event)
                        .subscribeOn(Schedulers.io())
                        .await()
                FirebaseMessaging
                        .getInstance()
                        .subscribeToTopic(event.eventId.toString())
                FirebaseMessaging
                        .getInstance()
                        .subscribeToTopic("subscribeEventSignIn-" + event.eventId)
                navigationController.goBack()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create event")
                _error.value = t
            }
        }
    }

    companion object {
        private val TAG = AddEventViewModel::class.toString()
    }
}
