package com.trainerapp.service

import android.app.Activity
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import com.trainerapp.models.Exceptions
import io.reactivex.Single

class PermissionServiceImpl(
        private val activity: Activity,
        private val rxPermissions: RxPermissions
) : PermissionService {

    override fun checkPermissions(vararg permissions: String): Single<Any> {
        return rxPermissions.request(*permissions).singleOrError()
                .onErrorResumeNext { Single.error(Exceptions.PermissionsDenied()) }
                .flatMap { granted ->
                    if (granted) {
                        Toast.makeText(activity, "Granted", Toast.LENGTH_SHORT).show()
                        Single.just(true)
                    } else {
                        checkPermissionsRationale(*permissions).flatMap {
                            if (it) {
                                Toast.makeText(activity, "Denied", Toast.LENGTH_SHORT).show()
                                Single.error<Any>(Exceptions.PermissionsDenied())
                            } else {
                                Toast.makeText(activity, "Denied forever", Toast.LENGTH_SHORT).show()
                                Single.error<Any>(Exceptions.PermissionsDeniedForever())
                            }
                        }
                    }
                }
    }

    private fun checkPermissionsRationale(vararg permissions: String): Single<Boolean> {
        return rxPermissions.shouldShowRequestPermissionRationale(activity, *permissions).singleOrError()
    }
}
