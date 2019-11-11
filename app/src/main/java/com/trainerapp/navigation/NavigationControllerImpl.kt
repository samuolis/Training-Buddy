package com.trainerapp.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.trainerapp.enums.EventDetailScreen
import com.trainerapp.feature.add_event.AddEventDialogFragment
import com.trainerapp.manager.LoadingManager
import com.trainerapp.ui.fragments.*
import java.util.*

class NavigationControllerImpl(
        private val fragmentManager: FragmentManager,
        private val loadingManager: LoadingManager
) : NavigationController {

    private fun goToFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
                .add(android.R.id.content, fragment)
                .addToBackStack(fragment::class.toString() + UUID.randomUUID())
                .commit()
        fragmentManager.executePendingTransactions()
    }

    override fun goBack() {
        loadingManager.dismissEveryLoader()
        fragmentManager.popBackStack()
    }

    override fun showAccountEditDialogFragment() {
        val newFragment = AccountEditDialogFragment()
        goToFragment(newFragment)

    }

    override fun showEventCreateDialogFragment() {
        val newFragment = AddEventDialogFragment.newInstance()
        goToFragment(newFragment)
    }

    override fun showEventEditDialogFragment(eventId: Long) {
        val newFragment = AddEventDialogFragment.newInstance(eventId)
        goToFragment(newFragment)
    }

    override fun showEventSignedUsersListDialogFragment() {
        val newFragment = EventSignedUsersListDialogFragment()
        goToFragment(newFragment)
    }

    override fun showEventCommentsDialogFragment(eventId: Long) {
        val newFragment = EventCommentsDialogFragment.newInstance(eventId)
        goToFragment(newFragment)
    }

    override fun showDashnoardSearchDialogFragment() {
        val newFragment = SearchFragment()
        goToFragment(newFragment)
    }

    override fun showProfilePictureDialogFragment() {
        val newFragment = ProfilePictureDialogFragment()
        goToFragment(newFragment)
    }

    override fun showEventDetailsDialogFragment(eventId: Long, eventDetailScreen: EventDetailScreen) {
        val newFragment = EventDetailsDialogFragment.newInstance(
                eventId,
                eventDetailScreen
        )
        goToFragment(newFragment)
    }

    override fun showArchivedEventsDialogFragment() {
        val newFragment = ArchivedEventsDialogFragment()
        goToFragment(newFragment)
    }
}
