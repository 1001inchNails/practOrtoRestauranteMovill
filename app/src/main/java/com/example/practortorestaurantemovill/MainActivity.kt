package com.example.practortorestaurantemovill

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject


import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.crudform.SingleMenu
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.practortorestaurantemovill.ui.ChatFragment
import com.example.practortorestaurantemovill.ui.MenuFragment
import com.example.practortorestaurantemovill.network.WebSocketManager
import com.example.practortorestaurantemovill.ui.OnMenuActionsListener

class MainActivity : AppCompatActivity(), OnMenuActionsListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private var listaMenus: ArrayList<SingleMenu> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listaMenus = intent.getParcelableArrayListExtra<SingleMenu>("listaMenus")!!

        val mesaSeleccionada = intent.getIntExtra("mesaSeleccionada", -1)
        val mesa: String
        if (mesaSeleccionada != -1) {
            mesa = "Mesa" + mesaSeleccionada.toString()
            WebSocketManager.mesaSetter = mesa
        }else{
            mesa = "ERROR_ID_MESA"
        }

        //println(listaMenus)

        WebSocketManager.connect("ws://10.0.2.2:8025/websocket/$mesa")

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = MainPagerAdapter(this, listaMenus)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2 // para crear los 2 fragments a la vez y evitar problemas con la recepcion de mensajes

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Menu"
                1 -> "Chat"
                else -> "Tab ${position+1}"
            }
        }.attach()
    }

    override fun restartApp() {
        val intent = Intent(this, InicioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // WebSocketManager.disconnect()
    }
}


