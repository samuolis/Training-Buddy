package com.trainerapp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.di.component.DaggerActivityComponent
import com.trainerapp.di.module.ActivityModule

open class BaseActivity : AppCompatActivity() {

    private var _component: ActivityComponent? = null

    open val component: ActivityComponent
        get() {
            return _component!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _component = DaggerActivityComponent.builder()
                        .activityModule(ActivityModule(this))
                        .build()
        onInject(component)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }
}
