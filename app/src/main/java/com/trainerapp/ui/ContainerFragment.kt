package com.trainerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.trainerapp.R
import com.trainerapp.base.BaseFragment
import com.trainerapp.feature.maps.EventsMapFragment
import com.trainerapp.ui.fragments.DashboardFragment
import com.trainerapp.ui.fragments.HomeFragment
import com.trainerapp.ui.fragments.ProfileFragment
import kotlinx.android.synthetic.main.fragment_container.*

class ContainerFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation_view_pager.adapter = NavigationPagerAdapter(childFragmentManager)
        navigation_bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_map -> {
                    navigation_view_pager.currentItem = 0
                    true
                }
                R.id.navigation_home -> {
                    navigation_view_pager.currentItem = 1
                    true
                }
                R.id.navigation_dashboard -> {
                    navigation_view_pager.currentItem = 2
                    true
                }
                else -> {
                    navigation_view_pager.currentItem = 3
                    true
                }
            }
        }
    }


    inner class NavigationPagerAdapter(
            fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                NavigationTab.MAP.ordinal -> EventsMapFragment()
                NavigationTab.MY_EVENTS.ordinal -> HomeFragment()
                NavigationTab.GLOBAL.ordinal -> DashboardFragment()
                else -> ProfileFragment()
            }
        }

        override fun getCount() = NavigationTab.values().size

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                NavigationTab.MAP.ordinal -> "Maps"
                NavigationTab.MY_EVENTS.ordinal -> "My events"
                NavigationTab.GLOBAL.ordinal -> "Dashboard"
                else -> "Profile"
            }
        }
    }

    enum class NavigationTab {
        MAP, MY_EVENTS, GLOBAL, MY_PROFILE
    }
}
