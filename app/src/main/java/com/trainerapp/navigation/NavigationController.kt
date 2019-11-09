package com.trainerapp.navigation

import com.trainerapp.enums.EventDetailScreen

interface NavigationController {

    fun showAccountEditDialogFragment()

    fun showEventCreateDialogFragment()

    fun showEventEditDialogFragment(eventId: Long)

    fun showEventSignedUsersListDialogFragment()

    fun showEventCommentsDialogFragment(eventId: Long)

    fun showDashnoardSearchDialogFragment()

    fun showProfilePictureDialogFragment()

    fun showEventDetailsDialogFragment(eventId: Long, eventDetailScreen: EventDetailScreen)

    fun showArchivedEventsDialogFragment()
}
