package com.trainerapp.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import io.reactivex.Single
import java.io.IOException

class LocationServiceImpl(
        private val activity: Activity
) : LocationService {

    private val geocoder = Geocoder(activity.applicationContext)

    override fun getAddressByCoordinates(latitude: Double, longitude: Double): Single<List<Address>> {
        return Single.create {
            try {
                it.onSuccess(geocoder.getFromLocation(latitude, longitude, MAX_GEOCODER_RESULT))
            } catch (e: IOException) {
                it.tryOnError(IllegalStateException(e.message))
            }
        }
    }

    override fun getAddressByText(text: String): Single<List<Address>> {
        return Single.create {
            try {
                it.onSuccess(geocoder.getFromLocationName(text, MAX_GEOCODER_RESULT))
            } catch (e: IOException) {
                it.tryOnError(IllegalStateException(e.message))
            }
        }
    }

    override fun getDeviceLocation(): Single<Location> {
        return Single.create { emitter ->
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        emitter.onSuccess(location)
                    } else {
                        Log.e(TAG, "Your location is null")
                        emitter.onError(NoSuchElementException())
                    }
                }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Location not successful")
                            emitter.onError(exception)
                        }
            } else {
                Log.e(TAG, "Permission not granted")
                emitter.onError(IllegalAccessException())
            }
        }
    }

    companion object {

        private val TAG = LocationServiceImpl::class.toString()
        private const val MAX_GEOCODER_RESULT = 5
    }
}
