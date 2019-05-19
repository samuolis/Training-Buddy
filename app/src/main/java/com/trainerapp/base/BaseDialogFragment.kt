package com.trainerapp.base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.trainerapp.di.component.ActivityComponent

open class BaseDialogFragment : DialogFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onInject(activityComponent = (activity as BaseActivity).component)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }
}
