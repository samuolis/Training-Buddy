package com.trainerapp.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.trainerapp.ui.fragments.AccountEditDialogFragment
import com.trainerapp.ui.fragments.AddEventDialogFragment
import java.util.*

class NavigationControllerImpl(
        private val fragmentManager: FragmentManager
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

    override fun showAccountEditDialogFragment() {
        val newFragment = AccountEditDialogFragment()
        goToFragment(newFragment)

    }

    override fun showEventCreateDialogFragment() {
        val newFragment = AddEventDialogFragment.newInstance()
        goToFragment(newFragment)
    }
}
