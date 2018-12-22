package com.trainerapp.enums

import com.trainerapp.R

enum class ProfilePicture(drawableId: Int) {
    man(R.drawable.men),
    girl(R.drawable.girl);

    val drawableId :Int = drawableId


}