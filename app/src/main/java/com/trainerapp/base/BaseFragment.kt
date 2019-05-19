package com.trainerapp.base

import android.content.Context
import androidx.fragment.app.Fragment
import com.trainerapp.di.component.ActivityComponent

open class BaseFragment : Fragment() {

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onInject(activityComponent = (activity as BaseActivity).component)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }
}
