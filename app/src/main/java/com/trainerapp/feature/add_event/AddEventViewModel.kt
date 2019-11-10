package com.trainerapp.feature.add_event

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.trainerapp.base.BaseViewModel
import com.trainerapp.models.Event
import com.trainerapp.navigation.NavigationController
import com.trainerapp.service.LocationService
import com.trainerapp.web.webservice.EventWebService
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class AddEventViewModel @Inject constructor(
        private val locationService: LocationService,
        private val navigationController: NavigationController,
        private val eventWebService: EventWebService
) : BaseViewModel() {


    fun createEvent(event: Event) {
        launchWithProgress {
            try {
                eventWebService.createEvent(event).await()
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
