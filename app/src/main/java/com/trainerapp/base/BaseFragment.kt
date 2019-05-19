package com.trainerapp.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.trainerapp.di.component.ActivityComponent

open class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onInject(activityComponent = (activity as BaseActivity).component)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }
}
