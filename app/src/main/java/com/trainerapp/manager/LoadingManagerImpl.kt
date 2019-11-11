package com.trainerapp.manager

import com.trainerapp.base.BaseActivity
import com.trainerapp.dialog.LoaderDialog
import com.trainerapp.dialog.LoaderDialogBuilder

class LoadingManagerImpl(
        private val activity: BaseActivity
) : LoadingManager {

    private var loaderDialogs: MutableList<LoaderDialog> = mutableListOf()

    override fun setLoadingStatus(isLoading: Boolean) {
        if (isLoading) {
            loaderDialogs.add(LoaderDialogBuilder(activity)
                    .show()
            )
        } else {
            dismissEveryLoader()
        }
    }

    override fun dismissEveryLoader() {
        loaderDialogs.forEach {
            it.dismiss()
        }
    }
}
