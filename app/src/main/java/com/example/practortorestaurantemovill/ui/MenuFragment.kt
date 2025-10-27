package com.example.practortorestaurantemovill.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.practortorestaurantemovill.R
import com.example.crudform.SingleMenu

class MenuFragment : Fragment() {
    private lateinit var listaMenus: ArrayList<SingleMenu>

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

        // crear menus

    }
}