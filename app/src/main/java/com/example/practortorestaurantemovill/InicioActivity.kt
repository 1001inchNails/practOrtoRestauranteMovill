package com.example.practortorestaurantemovill

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.crudform.RespuestaAllMenus
import com.example.crudform.RespuestaEstadoMesa
import com.example.crudform.RetrofitClient
import com.example.crudform.SingleMenu
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioActivity : AppCompatActivity() {

    private lateinit var btnContinue: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerMesas: Spinner

    private var isLoadingComplete = false
    private var userClickedButton = false
    private val handler = Handler(Looper.getMainLooper())

    private var listaMenus: ArrayList<SingleMenu> = ArrayList()
    private var listaMesasDisponibles: ArrayList<String> = ArrayList()
    private var listaOpcionesMesasDisponibles: ArrayList<Int> = ArrayList()

    private var pendingMesaRequests = 0
    private var menusLoaded = false
    private var mesaSeleccionada: Int = -1 // valor por defecto (ninguna opcion)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_activity)

        btnContinue = findViewById(R.id.btnContinue)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        spinnerMesas = findViewById(R.id.spinnerMesas)

        setupUI()
        loadContent()
    }

    // montar UI
    private fun setupUI() {
        btnContinue.isEnabled = false
        btnContinue.text = "Continuar"

        // configurar el Spinner
        setupSpinner()

        btnContinue.setOnClickListener {
            if (mesaSeleccionada != -1) {
                userClickedButton = true
                readyCheck()
            } else {
                Toast.makeText(this, "Por favor, selecciona una mesa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // spinner para elegir mesa
    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ArrayList<String>())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMesas.adapter = adapter

        // listener para cuando se selecciona una opcion
        spinnerMesas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    mesaSeleccionada = selectedItem.toInt()
                    btnContinue.isEnabled = true
                } else {
                    mesaSeleccionada = -1
                    btnContinue.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mesaSeleccionada = -1
                btnContinue.isEnabled = false
            }
        }
    }

    private fun loadContent() {
        loadTuttiMenus()
        loadTuttiMesas()
    }

    // carga valores de mesas en lista, chequea disponibilidad
    private fun loadTuttiMesas() {
        val mesas = listOf("Mesa1", "Mesa2", "Mesa3", "Mesa4", "Mesa5")
        pendingMesaRequests = mesas.size

        mesas.forEach { mesaId ->
            verEstadoMesas(mesaId) { isAvailable ->
                if (isAvailable) {
                    listaMesasDisponibles.add(mesaId)
                }

                pendingMesaRequests--
                checkIfAllDataLoaded()
            }
        }
    }

    // carga opciones disponibles despues de criba
    private fun loadAllOpciones() {
        listaMesasDisponibles.forEach { mesaId ->
            if (mesaId == "Mesa1"){
                listaOpcionesMesasDisponibles.add(1)
            }
            if (mesaId == "Mesa2"){
                listaOpcionesMesasDisponibles.add(2)
            }
            if (mesaId == "Mesa3"){
                listaOpcionesMesasDisponibles.add(3)
            }
            if (mesaId == "Mesa4"){
                listaOpcionesMesasDisponibles.add(4)
            }
            if (mesaId == "Mesa5"){
                listaOpcionesMesasDisponibles.add(5)
            }
        }
        listaOpcionesMesasDisponibles.sort()

        // actualizar spinner con las opciones disponibles
        updateSpinner()
    }

    private fun updateSpinner() {
        val adapter = spinnerMesas.adapter as ArrayAdapter<String>
        adapter.clear()

        // primer elemento
        adapter.add("Selecciona una mesa")

        // agrega opciones disponibles
        listaOpcionesMesasDisponibles.forEach { mesa ->
            adapter.add(mesa.toString())
        }

        adapter.notifyDataSetChanged()
        spinnerMesas.isEnabled = true
    }

    // llamada api para comprobar disponibilidad de las mesas
    private fun verEstadoMesas(idMesa: String, callback: (Boolean) -> Unit) {
        RetrofitClient.instance.leerEstadoMesa(mesaId = idMesa).enqueue(object : Callback<RespuestaEstadoMesa> {
            override fun onResponse(call: Call<RespuestaEstadoMesa>, response: Response<RespuestaEstadoMesa>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    if (respuesta != null) {
                        when (respuesta.type) {
                            "success" -> {
                                val estadoMesa = respuesta.data
                                val estaOcupada = estadoMesa.ocupada
                                callback(!estaOcupada)
                            }
                            "failure" -> {
                                callback(false)
                                Toast.makeText(
                                    this@InicioActivity,
                                    "Error de BBDD",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                    Toast.makeText(
                        this@InicioActivity,
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RespuestaEstadoMesa>, t: Throwable) {
                callback(false)
                Toast.makeText(
                    this@InicioActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                System.out.println(t.message)
            }
        })
    }

    // llamada API para cargar tutti los menus
    private fun loadTuttiMenus() {
        RetrofitClient.instance.cargarMenus().enqueue(object : Callback<RespuestaAllMenus> {
            override fun onResponse(call: Call<RespuestaAllMenus>, response: Response<RespuestaAllMenus>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()

                    if (respuesta != null) {
                        when (respuesta.type) {
                            "success" -> {
                                val data: List<SingleMenu>? = response.body()?.data as List<SingleMenu>?

                                listaMenus.clear()

                                data?.forEach { modulo ->
                                    listaMenus.add(modulo)
                                }
                                menusLoaded = true
                                checkIfAllDataLoaded()
                            }

                            "failure" -> {
                                listaMenus.clear()
                                menusLoaded = true
                                checkIfAllDataLoaded()
                                Toast.makeText(
                                    this@InicioActivity,
                                    "Error de BBDD",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        listaMenus.clear()
                        menusLoaded = true
                        checkIfAllDataLoaded()
                    }
                } else {
                    listaMenus.clear()
                    menusLoaded = true
                    checkIfAllDataLoaded()
                    Toast.makeText(
                        this@InicioActivity,
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RespuestaAllMenus>, t: Throwable) {
                listaMenus.clear()
                menusLoaded = true
                checkIfAllDataLoaded()
                Toast.makeText(
                    this@InicioActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                System.out.println(t.message)
            }
        })
    }

    // chequeo por si faltan datos
    private fun checkIfAllDataLoaded() {
        if (pendingMesaRequests == 0 && menusLoaded) {
            loadAllOpciones()
            isLoadingComplete = true
            updateUI()

        }
    }

    // actualizar UI
    private fun updateUI() {
        tvStatus.text = "Carga completada"
        progressBar.visibility = View.GONE

        // mensaje por si no hay mesas disponibles
        if (listaOpcionesMesasDisponibles.isEmpty()) {
            tvStatus.text = "No hay mesas disponibles en este momento"
            spinnerMesas.isEnabled = false
        }
    }

    // chequeo de readiness para go al next stage of the aplicacion
    // comiuniqueison llea!
    private fun readyCheck() {
        if (isLoadingComplete && userClickedButton && mesaSeleccionada != -1) {
            goToMainActivity()
        }
    }

    // ir a menu principal de la aplicacion
    private fun goToMainActivity() {
        // cambia estado de la mesa a ocupado antes de ir a MainActivity
        cambiarEstadoMesa("Mesa$mesaSeleccionada", true) { exito ->
            if (exito) {
                // pasar datos para MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putParcelableArrayListExtra("listaMenus", listaMenus)
                intent.putStringArrayListExtra("listaMesasDisponibles", ArrayList(listaMesasDisponibles))
                intent.putExtra("mesaSeleccionada", mesaSeleccionada)
                startActivity(intent)
                finish()
            } else {
                // si falla, mostrar error y permitir reintentar
                Toast.makeText(this, "Error al ocupar la mesa. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                btnContinue.isEnabled = true
                userClickedButton = false
            }
        }
    }

    // cambiar el estado de la mesa
    private fun cambiarEstadoMesa(mesaId: String, ocupada: Boolean, callback: (Boolean) -> Unit) {
        RetrofitClient.instance.cambiarEstadoMesa(mesaId, ocupada).enqueue(object : Callback<RespuestaEstadoMesa> {
            override fun onResponse(call: Call<RespuestaEstadoMesa>, response: Response<RespuestaEstadoMesa>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    if (respuesta != null && respuesta.type == "success") {
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                    Toast.makeText(
                        this@InicioActivity,
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RespuestaEstadoMesa>, t: Throwable) {
                callback(false)
                Toast.makeText(
                    this@InicioActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}