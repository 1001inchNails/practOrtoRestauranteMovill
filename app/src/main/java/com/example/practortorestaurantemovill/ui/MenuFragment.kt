package com.example.practortorestaurantemovill.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.practortorestaurantemovill.R
import com.example.crudform.SingleMenu
import android.widget.*
import androidx.core.view.isVisible

class MenuFragment : Fragment() {
    private lateinit var listaMenus: ArrayList<SingleMenu>
    private val selectedItems = mutableListOf<SingleMenu>()

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

        scrollView.addView(linearLayout)
        menuRoot?.removeAllViews()
        menuRoot?.addView(scrollView)
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
            // Mostrar en consola los elementos seleccionados
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
        productContainer.addView(divider)

        return productContainer
    }
}