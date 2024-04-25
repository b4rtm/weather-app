package com.example.weatherapp;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weatherapp.fragments.AdditionalInfoFragment
import com.example.weatherapp.fragments.TodayFragment
import com.example.weatherapp.fragments.WeekForecastFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle

) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdditionalInfoFragment()
            1 -> TodayFragment()
            else -> WeekForecastFragment()
        }
    }
}
