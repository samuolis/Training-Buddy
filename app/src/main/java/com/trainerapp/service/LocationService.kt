package com.trainerapp.service

import android.location.Location
import io.reactivex.Single

interface LocationService {

    fun getDeviceLocation(): Single<Location>
}
