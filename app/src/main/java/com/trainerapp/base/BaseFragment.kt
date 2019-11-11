package com.trainerapp.base

import android.content.Context
import androidx.fragment.app.Fragment
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.manager.LoadingManager
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var loadingManager: LoadingManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onInject(activityComponent = (activity as BaseActivity).component)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }

    fun changeLoadingStatus(show: Boolean) {
        loadingManager.setLoadingStatus(show)
    }
}
