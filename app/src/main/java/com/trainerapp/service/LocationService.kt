package com.trainerapp.service

import android.location.Address
import android.location.Location
import io.reactivex.Single

interface LocationService {

    fun getDeviceLocation(): Single<Location>

    fun getAddressByCoordinates(latitude: Double, longitude: Double): Single<List<Address>>

    fun getAddressByText(text: String): Single<List<Address>>
}
