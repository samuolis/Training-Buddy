package com.trainerapp.service

import io.reactivex.Single

interface PermissionService {

    fun checkPermissions(vararg permissions: String): Single<Any>
}
