package com.example.practortorestaurantemovill

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InicioActivity : AppCompatActivity() {

    private lateinit var btnContinue: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar

    private var isLoadingComplete = false
    private var userClickedButton = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        // Inicializar vistas manualmente
        btnContinue = findViewById(R.id.btnContinue)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)

        setupUI()
        loadContent()
    }

    private fun setupUI() {
        // Inicialmente deshabilitar el botón
        btnContinue.isEnabled = false
        btnContinue.text = "Cargando..."

        btnContinue.setOnClickListener {
            userClickedButton = true
            checkIfReadyToProceed()
        }
    }

    private fun loadContent() {
        // Simular carga de contenido (3 segundos)
        handler.postDelayed({
            isLoadingComplete = true
            updateUIAfterLoading()
            checkIfReadyToProceed()
        }, 3000)
    }

    private fun updateUIAfterLoading() {
        btnContinue.isEnabled = true
        btnContinue.text = "Continuar"
        tvStatus.text = "¡Carga completada!"
    }

    private fun checkIfReadyToProceed() {
        if (isLoadingComplete && userClickedButton) {
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}