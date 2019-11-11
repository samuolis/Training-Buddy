package com.trainerapp.manager

interface LoadingManager {

    fun setLoadingStatus(isLoading: Boolean)

    fun dismissEveryLoader()
}
