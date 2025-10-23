package com.example.practortorestaurantemovill

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.practortorestaurantemovill.ui.MenuFragment
import com.example.practortorestaurantemovill.ui.ChatFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MenuFragment()
            1 -> ChatFragment()
            else -> MenuFragment()
        }
    }
}
