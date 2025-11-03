package com.example.practortorestaurantemovill.ui

import ProductosPagoAdapter
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.practortorestaurantemovill.R
import com.example.crudform.SingleMenu
import android.widget.*
import com.example.crudform.PedidoData
import com.example.crudform.PedidoItem
import com.example.crudform.RespuestaPedido
import com.example.crudform.RetrofitClient
import com.example.practortorestaurantemovill.network.WebSocketManager
import retrofit2.Call
import retrofit2.Response
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.example.crudform.RespuestaMesa
import retrofit2.Callback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crudform.RespuestaDelete
import com.example.crudform.RespuestaEstadoMesa


interface OnMenuActionsListener {
    fun restartApp()
}

class MenuFragment : Fragment() {
    private lateinit var listaMenus: ArrayList<SingleMenu>
    private val itemsSeleccionados = mutableListOf<SingleMenu>()

    private var hacerPedidoButton: Button? = null
    private var salirButton: Button? = null
    private var pagarButton: Button? = null

    private var mesa: String = ""

    private var pedidoPendiente = false

    private var pedidoConfirmado = false
    private var pedidoEnviadoExitosamente = false

    private var pedidoConfirmadoActivo = false

    private var loadingDialog: AlertDialog? = null

    private var listener: OnMenuActionsListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnMenuActionsListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // darle valor a lista menus (viene de InicioActivity) y valor de mesa
        listaMenus = arguments?.getParcelableArrayList<SingleMenu>("listaMenus") ?: ArrayList()
        //System.out.println(listaMenus)
        mesa = WebSocketManager.mesaGetter

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // reseteos iniciales
        actualizarEstadoCheckboxes()
        habilitarHacerPedido(false)
        actualizarEstadoBotonPagar()

        crearMenuDinamico()

        // obvserver para los estados de los pedidos
        WebSocketManager.pedidoConfirmado.observe(viewLifecycleOwner) { confirmado ->
            pedidoConfirmado = confirmado
            if (confirmado) {
                pedidoEnviadoExitosamente = true
                pedidoPendiente = false
                pedidoConfirmadoActivo = true
            }
            actualizarEstadoBotonSalir()
            actualizarEstadoCheckboxes()
            actualizarEstadoBotonPagar()
        }

        // observer para evento - pedido enviado a mesa
        WebSocketManager.systemEvents.observe(viewLifecycleOwner) { event ->
            if (event == "pedido_enviado_a_mesa") {
                habilitarHacerPedido(false)
                //mostrarOcultarPagar(true)
                pedidoPendiente = true
                //pedidoConfirmadoActivo = false
                actualizarEstadoCheckboxes()
                actualizarEstadoBotonPagar()
            }
        }

        // observer para evento - pedido cancelado desde restaurante
        WebSocketManager.systemEvents.observe(viewLifecycleOwner) { event ->
            if (event == "pedido_cancelado_a_mesa") {
                habilitarHacerPedido(false)
                pedidoPendiente = false
                pedidoConfirmadoActivo = false
                actualizarEstadoCheckboxes()
                actualizarEstadoBotonSalir()
                actualizarEstadoBotonPagar()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // asegurar que checkboxes estan en estado correcto cuando el fragment se reanude
        actualizarEstadoCheckboxes()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // limpiar estados residuales de checkboxes
        reiniciarCompletamenteCheckboxes()
    }

    // actualizar el estado de todos los checkboxes
    private fun actualizarEstadoCheckboxes() {
        val menuRoot = view?.findViewById<FrameLayout>(R.id.menuRoot)
        menuRoot?.let { root ->
            val checkBoxes = buscarCheckBoxes(root)
            val spinners = buscarSpinners(root)

            // actualizar checkboxes
            checkBoxes.forEach { checkBox ->
                checkBox.isEnabled = !pedidoPendiente
                if (pedidoPendiente) {
                    checkBox.setTextColor(Color.GRAY)
                } else {
                    checkBox.setTextColor(Color.BLACK)
                }
            }

            // actualizar spinners
            spinners.forEach { spinner ->
                spinner.isEnabled = !pedidoPendiente
            }
        }
    }

    // es visible si hay un pedido confirmado activo y no hay un pedido pendiente de confirmar o habia un pedido confirmado y se cancela un pedido pendiente
    private fun actualizarEstadoBotonPagar() {
        val mostrarPagar = (pedidoConfirmadoActivo && !pedidoPendiente) ||
                (pedidoConfirmado && !pedidoPendiente && !pedidoConfirmadoActivo)

        mostrarOcultarPagar(mostrarPagar)

        // ai mostramos el botón pagar, actualiza el estado de pedidoConfirmadoActivo
        if (mostrarPagar && pedidoConfirmado) {
            pedidoConfirmadoActivo = true
        }
    }

    private fun crearMenuDinamico() {
        val menuRoot = view?.findViewById<FrameLayout>(R.id.menuRoot)

        // scrollView para todos los elementos
        val scrollView = ScrollView(requireContext())
        scrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // linear layout vertical para organizar todo
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // elemento de menu para cada producto
        listaMenus.forEach { menuItem ->
            val productContainer = crearVistaProducto(menuItem)
            linearLayout.addView(productContainer)
        }

        // contenedor para los botones
        val buttonContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        }

        // boton "Hacer Pedido" (deshabilitado por defecto)
        hacerPedidoButton = Button(requireContext()).apply {
            text = "Hacer Pedido"
            isEnabled = false

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            setOnClickListener {
                botonHacerPedido()
            }
        }

        // boton "Pagar" (deshabilitado por defecto)
        pagarButton = Button(requireContext()).apply {
            text = "Pagar"
            visibility = View.GONE

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )


            setOnClickListener {
                botonPagarPedidos()
            }
        }

        // boton "Salir"
        salirButton = Button(requireContext()).apply {
            text = "Salir"
            visibility = View.VISIBLE
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            setOnClickListener {
                botonSalir()
            }

        }

        // agregar botones al contenedor
        buttonContainer.addView(hacerPedidoButton)
        buttonContainer.addView(salirButton)
        buttonContainer.addView(pagarButton)

        // agregar al ScrollView
        linearLayout.addView(buttonContainer)
        scrollView.addView(linearLayout)

        menuRoot?.removeAllViews()
        menuRoot?.addView(scrollView)
    }

    private fun botonHacerPedido() {
        // verificar que no haya pedido pendiente (redundante por si acaso)
        if (pedidoPendiente) {
            Toast.makeText(requireContext(), "Ya hay un pedido pendiente de confirmar", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarOcultarSalir(false)
        Toast.makeText(requireContext(), "Procesando pedido...", Toast.LENGTH_SHORT).show()

        // Establecer que hay un pedido pendiente
        pedidoPendiente = true
        actualizarEstadoCheckboxes()
        habilitarHacerPedido(false)
        actualizarEstadoBotonPagar() // Actualizar estado del botón pagar

        // enviar el pedido y notificar
        enviarPedidoDeMesa(
            mesa = mesa,
            pedidos = itemsSeleccionados,
            callback = { exito ->
                if (exito) {
                    //System.out.println("pedido guardado")
                    itemsSeleccionados.clear()
                    desmarcarCheckboxesYResetearSpinners()
                    WebSocketManager.sendPedidoMensaje("Pedido pendiente")
                } else {
                    //System.out.println("pedido no guardado")
                    pedidoPendiente = false
                    actualizarEstadoCheckboxes()
                    habilitarHacerPedido(itemsSeleccionados.isNotEmpty())
                    if (!pedidoEnviadoExitosamente) {
                        mostrarOcultarSalir(true)
                    }
                    actualizarEstadoBotonPagar()
                }
            }
        )
    }

    // para el reinicio
    private fun reiniciarCompletamenteCheckboxes() {
        val menuRoot = view?.findViewById<FrameLayout>(R.id.menuRoot)
        menuRoot?.let { root ->
            val checkBoxes = buscarCheckBoxes(root)
            val spinners = buscarSpinners(root)

            // reiniciar checkboxes
            checkBoxes.forEach { checkBox ->
                checkBox.isChecked = false
                checkBox.isEnabled = true
                checkBox.setTextColor(Color.BLACK)
            }

            // reiniciar spinners
            spinners.forEach { spinner ->
                spinner.setSelection(0)
                spinner.isEnabled = true
            }
        }

        // reiniciar las cantidades en lista menus
        listaMenus.forEach { menuItem ->
            menuItem.cantidad = 1
        }

        // reiniciar items seleccionados
        itemsSeleccionados.clear()
        habilitarHacerPedido(false)
    }

    // al recibir confirmacion de pedido
    private fun desmarcarCheckboxesYResetearSpinners() {
        val menuRoot = view?.findViewById<FrameLayout>(R.id.menuRoot)
        menuRoot?.let { root ->
            val checkBoxes = buscarCheckBoxes(root)
            val spinners = buscarSpinners(root)

            // pos eso
            checkBoxes.forEach { checkBox ->
                checkBox.isChecked = false
                checkBox.isEnabled = !pedidoPendiente
                if (pedidoPendiente) {
                    checkBox.setTextColor(Color.GRAY)
                } else {
                    checkBox.setTextColor(Color.BLACK)
                }
            }

            // y esto tambien
            spinners.forEach { spinner ->
                spinner.setSelection(0)
                spinner.isEnabled = !pedidoPendiente
            }
        }

        // y tal y cual
        listaMenus.forEach { menuItem ->
            menuItem.cantidad = 1
        }

        itemsSeleccionados.clear()
        habilitarHacerPedido(false)
    }


    // devuelve los checkboxes
    private fun buscarCheckBoxes(view: View): List<CheckBox> {
        val checkBoxes = mutableListOf<CheckBox>()

        when (view) {
            is CheckBox -> {
                checkBoxes.add(view)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    checkBoxes.addAll(buscarCheckBoxes(child))
                }
            }
        }

        return checkBoxes
    }

    // este te piratea la NASA, si te parece
    private fun buscarSpinners(view: View): List<Spinner> {
        val spinners = mutableListOf<Spinner>()

        when (view) {
            is Spinner -> {
                spinners.add(view)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    spinners.addAll(buscarSpinners(child))
                }
            }
        }

        return spinners
    }

    // salir y reiniciar mesa
    private fun botonSalir() {
        // reinicia estado de la mesa a no ocupada antes de salir
        cambiarEstadoMesa(mesa, false) { exito ->
            if (exito) {
                println("Mesa libre")
            } else {
                println("Error al librar mesa")
            }
            reiniciarEstados()
            WebSocketManager.disconnect()
            listener?.restartApp()
        }
    }


    // habilitar/deshabilitar el boton "Hacer Pedido"
    fun habilitarHacerPedido(estaHabilitado: Boolean) {
        hacerPedidoButton?.isEnabled = estaHabilitado
    }


    // mostrar/ocultar el boton "Pagar"
    fun mostrarOcultarPagar(mostrar: Boolean) {
        pagarButton?.visibility = if (mostrar) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    // mostrar/ocultar el boton "Salir"
    fun mostrarOcultarSalir(mostrar: Boolean) {
        salirButton?.visibility = if (mostrar) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    private fun actualizarEstadoBotonSalir() {
        mostrarOcultarSalir(!pedidoEnviadoExitosamente)
    }


    private fun crearVistaProducto(menuItem: SingleMenu): View {
        // contenedor para cada producto
        val productContainer = LinearLayout(requireContext())
        productContainer.orientation = LinearLayout.VERTICAL
        productContainer.setPadding(0, 16, 0, 16)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 8, 0, 8)
        productContainer.layoutParams = layoutParams

        // linea divisoria
        val divider = View(requireContext())
        divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        dividerParams.setMargins(0, 8, 0, 8)
        divider.layoutParams = dividerParams

        // layout horizontal para las movidas
        val topRow = LinearLayout(requireContext())
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // TextView para nombre
        val nameTextView = TextView(requireContext())
        nameTextView.text = menuItem.nombre
        nameTextView.textSize = 18f
        nameTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        val nameParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        nameParams.weight = 1f
        nameTextView.layoutParams = nameParams

        // TextView para precio
        val priceTextView = TextView(requireContext())
        priceTextView.text = "${menuItem.precio}€"
        priceTextView.textSize = 16f
        priceTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        priceTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))

        // CheckBox para seleccionar producto
        val checkBox = CheckBox(requireContext())
        checkBox.text = "Seleccionar"

        // iniciar estado del checkbox basado en pedidoPendiente
        checkBox.isEnabled = !pedidoPendiente
        if (pedidoPendiente) {
            checkBox.setTextColor(Color.GRAY)
        }


        // listener para pedido pendiente
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // procesar cambios si no hay pedido pendiente
            if (!pedidoPendiente) {
                if (isChecked) {
                    itemsSeleccionados.add(menuItem)
                } else {
                    itemsSeleccionados.remove(menuItem)
                }

                // habilitar boton "Hacer Pedido" solo si hay items seleccionados
                habilitarHacerPedido(itemsSeleccionados.isNotEmpty())

                //println("Productos seleccionados: $itemsSeleccionados")
            } else {
                // si hay pedido pendiente, evitar que se marque/desmarque
                checkBox.isChecked = false
            }
        }

        // agregar elementos a fila superior
        topRow.addView(nameTextView)
        topRow.addView(priceTextView)
        topRow.addView(checkBox)

        // layout horizontal para descripcion y cantidad
        val bottomRow = LinearLayout(requireContext())
        bottomRow.orientation = LinearLayout.HORIZONTAL
        bottomRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // TextView para la descripcion
        val descriptionTextView = TextView(requireContext())
        descriptionTextView.text = menuItem.descripcion
        descriptionTextView.textSize = 14f
        descriptionTextView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        descriptionTextView.setPadding(0, 8, 0, 0)
        val descriptionParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        descriptionParams.weight = 1f
        descriptionTextView.layoutParams = descriptionParams

        // contenedor para label y spinner de cantidad
        val cantidadContainer = LinearLayout(requireContext())
        cantidadContainer.orientation = LinearLayout.HORIZONTAL
        cantidadContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // label "Unidades"
        val unidadesLabel = TextView(requireContext())
        unidadesLabel.text = "Unidades:"
        unidadesLabel.textSize = 14f
        unidadesLabel.setPadding(0, 8, 8, 0)
        val labelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        unidadesLabel.layoutParams = labelParams

        // spinner para la cantidad
        val cantidadSpinner = Spinner(requireContext())
        val cantidades = (1..10).toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cantidades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cantidadSpinner.adapter = adapter

        // valor por defecto
        cantidadSpinner.setSelection(menuItem.cantidad - 1)

        // listener para cambios en la cantidad
        cantidadSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                menuItem.cantidad = cantidades[position]
                // actualizar el item en itemsSeleccionados si ya esta seleccionado
                val index = itemsSeleccionados.indexOfFirst { it.id == menuItem.id }
                if (index != -1) {
                    itemsSeleccionados[index].cantidad = menuItem.cantidad
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // pos eso
            }
        }

        // params del spinner
        val spinnerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cantidadSpinner.layoutParams = spinnerParams

        // agregar label y spinner al contenedor de cantidad
        cantidadContainer.addView(unidadesLabel)
        cantidadContainer.addView(cantidadSpinner)

        // agregar descripcion y contenedor de cantidad a la fila inferior
        bottomRow.addView(descriptionTextView)
        bottomRow.addView(cantidadContainer)

        // agregar todos los elementos a contenedor de producto
        productContainer.addView(topRow)
        productContainer.addView(bottomRow)
        //productContainer.addView(divider)

        return productContainer
    }

    // envia pedido de la mesa al ristorante
    private fun enviarPedidoDeMesa(mesa: String, pedidos: List<SingleMenu>, callback: (Boolean) -> Unit) {
        // crear el objeto de datos para el pedido
        val pedidoData = PedidoData(
            mesa = mesa,
            pedidos = pedidos.map { menuItem ->
                PedidoItem(
                    id = menuItem.id,
                    nombre = menuItem.nombre,
                    precio = menuItem.precio,
                    cantidad = menuItem.cantidad,
                    descripcion = menuItem.descripcion
                )
            }
        )

        // guarda pedidos en BBDD
        RetrofitClient.instance.mandarPedido(pedidoData).enqueue(object : retrofit2.Callback<RespuestaPedido> {
            override fun onResponse(call: Call<RespuestaPedido>, response: Response<RespuestaPedido>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    if (respuesta != null) {
                        when (respuesta.type) {
                            "success" -> {
                                callback(true)
                                Toast.makeText(
                                    requireContext(),
                                    "Pedido enviado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            "failure" -> {
                                callback(false)
                                Toast.makeText(
                                    requireContext(),
                                    "Error al enviar pedido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        callback(false)
                        Toast.makeText(
                            requireContext(),
                            "Respuesta vacía del servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    callback(false)
                    Toast.makeText(
                        requireContext(),
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<RespuestaPedido>, t: Throwable) {
                callback(false)
                Toast.makeText(
                    requireContext(),
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                System.out.println(t.message)
            }
        })
    }

    // abre modal para finiquitar cuenta, cambiar estados y reiniciar app
    private fun botonPagarPedidos() {

        // devuelve los productos servidos en mesa (haSidoServido: true)
        obtenerProductosServidos { productosServidos ->
            if (productosServidos.isNotEmpty()) {
                mostrarModalPago(productosServidos)
            } else {
                Toast.makeText(requireContext(), "No hay productos servidos para pagar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // llamada API para obtener pedidos servidos en mesa
    private fun obtenerProductosServidos(callback: (List<PedidoItem>) -> Unit) {
        RetrofitClient.instance.leerMesa(mesa).enqueue(object : retrofit2.Callback<RespuestaMesa> {
            override fun onResponse(call: Call<RespuestaMesa>, response: Response<RespuestaMesa>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    val documentosMesa = respuesta?.data ?: emptyList()

                    // devuelve pedidos de todos los documentos de mesa que se hayan servido
                    val todosLosPedidos = documentosMesa.flatMap { documento ->
                        if (documento.haSidoServido == true) {
                            documento.pedidos ?: emptyList()
                        } else {
                            emptyList()
                        }
                    }

                    if (todosLosPedidos.isNotEmpty()) {
                        // yay!
                        callback(todosLosPedidos)
                    } else {
                        // nay...
                        callback(emptyList())
                    }
                } else {
                    // sad trombone noise
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<RespuestaMesa>, t: Throwable) {
                callback(emptyList())
            }
        })
    }

    // montaje de modal de pago
    private fun mostrarModalPago(productosServidos: List<PedidoItem>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pago, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewProductos)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnPagar = dialogView.findViewById<Button>(R.id.btnPagar)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotal)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ProductosPagoAdapter(productosServidos)
        recyclerView.adapter = adapter

        val total = productosServidos.sumOf { producto ->
            try {
                val precio = producto.precio?.toDouble() ?: 0.0
                val cantidad = producto.cantidad?.toInt() ?: 0
                precio * cantidad
            } catch (e: Exception) {
                // valor de retorno si vamos full kaput
                0.0
            }
        }

        tvTotal.text = "Total: ${String.format("%.2f", total)}€"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Factura")
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnPagar.setOnClickListener {
            // deshabilitar botones para evitar multi-clicks
            btnPagar.isEnabled = false
            btnCancelar.isEnabled = false
            procesarPago(dialog)
        }

        dialog.show()
    }

    private fun procesarPago(dialog: AlertDialog) {
        // restaura estado de mesa a disponible al pagar
        cambiarEstadoMesa(mesa, false) { exito ->
            if (exito) {
                println("Oh yeah!")
            } else {
                println("Oh no!")
            }

            reiniciarEstados()
            mostrarOcultarSalir(false)
            actualizarEstadoBotonPagar()

            // notificar a desktop
            WebSocketManager.send("Mesa despachada", "chat")

            mostrarLoading()

            // llamada API para borrar todos los datos de pedidos de la mesa
            RetrofitClient.instance.deleteMesa(mesa).enqueue(object : retrofit2.Callback<RespuestaDelete> {
                override fun onResponse(call: Call<RespuestaDelete>, response: Response<RespuestaDelete>) {
                    if (response.isSuccessful && response.body()?.type == "success") {
                        dialog.dismiss()

                        reiniciarCompletamenteCheckboxes()

                        // delay antes de reiniciar
                        Handler(Looper.getMainLooper()).postDelayed({
                            ocultarLoading()
                            activity?.supportFragmentManager?.beginTransaction()?.detach(this@MenuFragment)?.attach(this@MenuFragment)?.commit()
                            listener?.restartApp()
                        }, 2000)
                    } else {
                        ocultarLoading()
                        Toast.makeText(requireContext(), "Error al procesar pago", Toast.LENGTH_SHORT).show()
                        dialog.findViewById<Button>(R.id.btnPagar)?.isEnabled = true
                        dialog.findViewById<Button>(R.id.btnCancelar)?.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<RespuestaDelete>, t: Throwable) {
                    ocultarLoading()
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                    dialog.findViewById<Button>(R.id.btnPagar)?.isEnabled = true
                    dialog.findViewById<Button>(R.id.btnCancelar)?.isEnabled = true
                }
            })
        }
    }

    private fun mostrarLoading() {
        val loadingView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(loadingView)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun ocultarLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun reiniciarEstados() {
        pedidoPendiente = false
        pedidoConfirmado = false
        pedidoEnviadoExitosamente = false
        pedidoConfirmadoActivo = false
        itemsSeleccionados.clear()

        reiniciarCompletamenteCheckboxes()

        actualizarEstadoCheckboxes()
        habilitarHacerPedido(false)
        actualizarEstadoBotonPagar()
        actualizarEstadoBotonSalir()
    }


    // llamada API para cambiar estado de ocupacion de la mesa
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
                }
            }

            override fun onFailure(call: Call<RespuestaEstadoMesa>, t: Throwable) {
                callback(false)
            }
        })
    }
}