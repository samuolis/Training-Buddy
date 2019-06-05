package com.trainerapp.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.readOnly
import com.trainerapp.extension.toSingle
import com.trainerapp.models.Event
import com.trainerapp.ui.NavigationActivity
import com.trainerapp.web.webservice.EventWebService
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class EventDetailsViewModel @Inject constructor(
        private val eventWebService: EventWebService,
        private val activity: Activity
) : BaseViewModel() {

    private val TAG = EventDetailsViewModel::class.simpleName

    private var _detailsOneEvent = MutableLiveData<Event>()
    val detailsOneEvent = _detailsOneEvent.readOnly()

    private val schedulerIO = Schedulers.io()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun loadDetailsEvent(eventId: Long?) {
        if (eventId == null) return

        launch {
            try {
                _detailsOneEvent.value = eventWebService.getEventById(eventId)
                        .subscribeOn(schedulerIO)
                        .await()
            } catch (t: Throwable) {
                Log.e(TAG, "failed to get event")
                _error.postValue(t)
            }
        }
    }

    fun signEvent(userId: String, eventId: Long?) {
        if (currentUser == null) return
        currentUser.getIdToken(true).toSingle()
                .subscribeOn(schedulerIO)
                .flatMap { task ->
                    val token = task.token
                    eventWebService.signEvent(userId, eventId, token)
                            .subscribeOn(schedulerIO)
                }
                .subscribeBy(
                        onSuccess = {
                            (activity as NavigationActivity).backOnStack()
                            FirebaseMessaging.getInstance().subscribeToTopic(eventId.toString())
                        },
                        onError = {
                            Log.e(TAG, "failed to sign event")
                            _error.postValue(it)
                        }
                )
                .bind()
    }
}
