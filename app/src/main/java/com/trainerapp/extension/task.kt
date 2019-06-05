package com.trainerapp.extension

import com.google.android.gms.tasks.Task
import io.reactivex.Single

fun <T : Any> Task<T>.toSingle(): Single<T> {
    return Single.create { emitter ->
        this.addOnSuccessListener { result ->
            if (!emitter.isDisposed) {
                emitter.onSuccess(result)
            }
        }.addOnFailureListener { exception ->
            if (!emitter.isDisposed) {
                emitter.tryOnError(exception)
            }
        }
    }
}
