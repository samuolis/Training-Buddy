package com.trainerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.trainerapp.base.BaseViewModel
import com.trainerapp.extension.readOnly
import com.trainerapp.models.Event
import com.trainerapp.web.webservice.EventWebService
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class EventDetailsViewModel @Inject constructor(
        private val eventWebService: EventWebService
) : BaseViewModel() {

    private val TAG = EventDetailsViewModel::class.simpleName
    private var _detailsOneEvent = MutableLiveData<Event>()
    val detailsOneEvent = _detailsOneEvent.readOnly()

    val schedulerIO = Schedulers.io()

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
}
