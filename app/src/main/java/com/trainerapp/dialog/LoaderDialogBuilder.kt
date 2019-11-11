package com.trainerapp.dialog

import android.content.Context

class LoaderDialogBuilder(private val context: Context) {

    fun show(): LoaderDialog {
        return LoaderDialog(context)
                .apply { show() }
    }
}
