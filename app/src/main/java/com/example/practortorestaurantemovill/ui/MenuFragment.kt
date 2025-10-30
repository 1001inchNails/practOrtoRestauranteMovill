package com.example.practortorestaurantemovill.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.practortorestaurantemovill.R
import com.example.crudform.SingleMenu
import android.widget.*
import androidx.core.view.isVisible
import com.example.practortorestaurantemovill.network.WebSocketManager

interface OnMenuActionsListener {
    fun restartApp()
}

class MenuFragment : Fragment() {
    private lateinit var listaMenus: ArrayList<SingleMenu>
    private val selectedItems = mutableListOf<SingleMenu>()

    private var hacerPedidoButton: Button? = null
    private var salirButton: Button? = null
    private var pagarButton: Button? = null

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
        listaMenus = arguments?.getParcelableArrayList<SingleMenu>("listaMenus") ?: ArrayList()
        System.out.println(listaMenus)

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

        crearMenuDinamico()
    }

    private fun crearMenuDinamico() {
        val menuRoot = view?.findViewById<FrameLayout>(R.id.menuRoot)

        // Crear un ScrollView para contener todos los elementos
        val scrollView = ScrollView(requireContext())
        scrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // Crear un LinearLayout vertical para organizar los productos
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Crear un elemento de menú para cada producto
        listaMenus.forEach { menuItem ->
            val productContainer = createProductView(menuItem)
            linearLayout.addView(productContainer)
        }

        // Crear contenedor para los botones
        val buttonContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        }

        // Crear botón "Hacer Pedido" (deshabilitado por defecto)
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

        // Crear botón "Pagar" (deshabilitado por defecto)
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

        // Crear botón "Salir"
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

        // Agregar botones al contenedor
        buttonContainer.addView(hacerPedidoButton)
        buttonContainer.addView(salirButton)
        buttonContainer.addView(pagarButton)

        // Agregar al ScrollView
        linearLayout.addView(buttonContainer)
        scrollView.addView(linearLayout)

        menuRoot?.removeAllViews()
        menuRoot?.addView(scrollView)
    }

    private fun botonHacerPedido() {
        mostrarOcultarSalir(false)
        Toast.makeText(requireContext(), "Procesando pedido...", Toast.LENGTH_SHORT).show()
    }

    private fun botonSalir() {
        WebSocketManager.disconnect()
        listener?.restartApp()
    }

    private fun botonPagarPedidos() {
        mostrarOcultarSalir(false)
        Toast.makeText(requireContext(), "Procesando pedido...", Toast.LENGTH_SHORT).show()
    }

    // Función para habilitar/deshabilitar el botón "Hacer Pedido"
    fun habilitarHacerPedido(estaHabilitado: Boolean) {
        hacerPedidoButton?.isEnabled = estaHabilitado
    }

    // Función para mostrar/ocultar el botón "Pagar"
    fun mostrarOcultarPagar(mostrar: Boolean) {
        pagarButton?.visibility = if (mostrar) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    // Función para mostrar/ocultar el botón "Salir"
    fun mostrarOcultarSalir(mostrar: Boolean) {
        salirButton?.visibility = if (mostrar) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun createProductView(menuItem: SingleMenu): View {
        // Crear un contenedor para cada producto
        val productContainer = LinearLayout(requireContext())
        productContainer.orientation = LinearLayout.VERTICAL
        productContainer.setPadding(0, 16, 0, 16)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 8, 0, 8)
        productContainer.layoutParams = layoutParams

        // Crear una línea divisoria
        val divider = View(requireContext())
        divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        dividerParams.setMargins(0, 8, 0, 8)
        divider.layoutParams = dividerParams

        // Crear layout horizontal para nombre, precio y checkbox
        val topRow = LinearLayout(requireContext())
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // TextView para el nombre
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

        // TextView para el precio
        val priceTextView = TextView(requireContext())
        priceTextView.text = "$${menuItem.precio}"
        priceTextView.textSize = 16f
        priceTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        priceTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))

        // CheckBox para seleccionar el producto
        val checkBox = CheckBox(requireContext())
        checkBox.text = "Seleccionar"
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(menuItem)
            } else {
                selectedItems.remove(menuItem)
            }
            if (selectedItems.isNotEmpty()){
                habilitarHacerPedido(true)
            }else{
                habilitarHacerPedido(false)
            }


            println("Productos seleccionados: $selectedItems")
        }

        // Agregar elementos a la fila superior
        topRow.addView(nameTextView)
        topRow.addView(priceTextView)
        topRow.addView(checkBox)

        // TextView para la descripción
        val descriptionTextView = TextView(requireContext())
        descriptionTextView.text = menuItem.descripcion
        descriptionTextView.textSize = 14f
        descriptionTextView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        descriptionTextView.setPadding(0, 8, 0, 0)

        // Agregar todos los elementos al contenedor del producto
        productContainer.addView(topRow)
        productContainer.addView(descriptionTextView)
        //productContainer.addView(divider)

        return productContainer
    }
}