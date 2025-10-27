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

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private var listaMenus: ArrayList<SingleMenu> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listaMenus = intent.getParcelableArrayListExtra<SingleMenu>("listaMenus")!!

        //println(listaMenus)

        WebSocketManager.connect("ws://10.0.2.2:8025/websocket/android-client")

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = MainPagerAdapter(this, listaMenus)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Menu"
                1 -> "Chat"
                else -> "Tab ${position+1}"
            }
        }.attach()
    }

    private fun restartApp() {
        // Crear intent para volver a LoadingActivity
        val intent = Intent(this, InicioActivity::class.java)

        // Limpiar toda la pila de actividades y crear una nueva
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Cerrar WebSocket si es necesario
        //WebSocketManager.disconnect()

        startActivity(intent)
        finish()

        // Forzar cierre del proceso actual (opcional)
        // android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onDestroy() {
        super.onDestroy()
        // WebSocketManager.disconnect()
    }
}


