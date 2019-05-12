package com.trainerapp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.trainerapp.di.component.ActivityComponent
import com.trainerapp.di.component.DaggerActivityComponent
import com.trainerapp.di.module.ActivityModule

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityComponent: ActivityComponent =
                DaggerActivityComponent.builder()
                        .activityModule(ActivityModule(this))
                        .build()
        onInject(activityComponent)
    }

    open fun onInject(activityComponent: ActivityComponent) {
    }
}