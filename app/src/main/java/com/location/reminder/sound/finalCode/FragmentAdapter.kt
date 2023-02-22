package com.location.reminder.sound.finalCode

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.location.reminder.sound.finalCode.model.TabItem
import com.location.reminder.sound.finalCode.ui.fragments.RemindersFragment

class FragmentAdapter(activity: FragmentActivity, val tabList: Array<TabItem>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return tabList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            position -> tabList[position].fragment
            else -> RemindersFragment()
        }
    }

}