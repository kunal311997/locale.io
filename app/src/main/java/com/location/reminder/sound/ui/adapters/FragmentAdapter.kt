package com.location.reminder.sound.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.location.reminder.sound.ui.fragments.RemindersFragment

class FragmentAdapter(
    activity: FragmentActivity,
    private val tabList: Array<Pair<String, RemindersFragment>>
) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return tabList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            position -> tabList[position].second
            else -> RemindersFragment()
        }
    }

}