package com.example.practortorestaurantemovill

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.crudform.SingleMenu
import com.example.practortorestaurantemovill.ui.MenuFragment
import com.example.practortorestaurantemovill.ui.ChatFragment

class MainPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val listaMenus: ArrayList<SingleMenu>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = MenuFragment()
                val args = Bundle()
                args.putParcelableArrayList("listaMenus", listaMenus)
                fragment.arguments = args
                fragment
            }
            1 -> ChatFragment()
            else -> MenuFragment()
        }
    }
}
