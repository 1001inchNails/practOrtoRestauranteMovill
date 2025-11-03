package com.example.practortorestaurantemovill.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.json.JSONObject
import com.example.practortorestaurantemovill.R
import com.example.practortorestaurantemovill.network.WebSocketManager

class ChatFragment : Fragment() {

    private lateinit var messagesView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView

    // regex para letras, acentos, espacios y signos de puntuación basicos
    private val spanishTextRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s.,!?¿¡]*\$".toRegex()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val valor = inflater.inflate(R.layout.fragment_chat, container, false)
        messagesView = valor.findViewById(R.id.messagesView)
        messageInput = valor.findViewById(R.id.messageInput)
        sendButton = valor.findViewById(R.id.sendButton)
        scrollView = valor.findViewById(R.id.scrollView)

        // validacion en tiempo real
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validarInput()
            }
        })

        WebSocketManager.mensajesEntrantes.observe(viewLifecycleOwner, Observer { msg ->
            appendMensaje(msg)
        })

        WebSocketManager.connectionStatus.observe(viewLifecycleOwner, Observer { status ->
            appendMensaje("$status")
        })

        sendButton?.setOnClickListener {
            val mnsj = messageInput.text.toString().trim()
            if (WebSocketManager.mesaGetter.isNotEmpty() && esInputValido(mnsj)) {
                WebSocketManager.send(mnsj, "chat")
                appendMensaje("Yo: $mnsj")
                messageInput.setText("")
            }
        }

        sendButton.isEnabled = false

        return valor
    }

    private fun validarInput() {
        val text = messageInput.text.toString().trim()
        val isValid = esInputValido(text)

        sendButton.isEnabled = isValid && text.isNotBlank()

        if (text.isNotEmpty() && !isValid) {
            messageInput.error = "Caracter no valido"
        } else {
            messageInput.error = null
        }
    }

    private fun esInputValido(text: String): Boolean {
        return spanishTextRegex.matches(text)
    }

    private fun appendMensaje(newMsg: String) {
        messagesView.append(newMsg + "\n")
        // scroll
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}