package com.trainerapp.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import io.reactivex.Single

class LocationServiceImpl(
        private val activity: Activity
) : LocationService {

    private val TAG = LocationServiceImpl::class.simpleName

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
}
