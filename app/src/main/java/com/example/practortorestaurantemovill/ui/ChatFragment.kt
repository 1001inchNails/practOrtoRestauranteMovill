package com.example.practortorestaurantemovill.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
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
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import org.json.JSONObject
import com.example.practortorestaurantemovill.R
import com.example.practortorestaurantemovill.network.WebSocketManager

class ChatFragment : Fragment() {

    private lateinit var messagesView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView

    private var colorPropio: Int = 0
    private var colorRestaurante: Int = 0
    private var colorSistema: Int = 0
    private var colorError: Int = 0

    // regex para letras, acentos, espacios y signos de puntuación basicos
    private val spanishTextRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s.,!?¿¡]*\$".toRegex()

    // sonido alerta
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val valor = inflater.inflate(R.layout.fragment_chat, container, false)
        messagesView = valor.findViewById(R.id.messagesView)
        messageInput = valor.findViewById(R.id.messageInput)
        sendButton = valor.findViewById(R.id.sendButton)
        scrollView = valor.findViewById(R.id.scrollView)

        colorPropio = ContextCompat.getColor(requireContext(), R.color.color_mensaje_propio)
        colorRestaurante = ContextCompat.getColor(requireContext(), R.color.color_mensaje_restaurante)
        colorSistema = ContextCompat.getColor(requireContext(), R.color.color_mensaje_sistema)
        colorError = ContextCompat.getColor(requireContext(), R.color.color_mensaje_error)

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.notificacion2)

        // validacion en tiempo real
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validarInput()
            }
        })

        WebSocketManager.mensajesEntrantes.observe(viewLifecycleOwner, Observer { msg ->
            appendMensajeConColor(msg)
            // para que solo reproduzca alerta sonido con mensajes de chat entrantes
            if (!msg.startsWith("PROPIO:") && esMensajeDeChatValido(msg)) {
                reproducirSonidoAlerta()
            }
        })

        WebSocketManager.connectionStatus.observe(viewLifecycleOwner, Observer { status ->
            appendMensajeSistema("$status")
        })

        sendButton?.setOnClickListener {
            val mnsj = messageInput.text.toString().trim()
            if (WebSocketManager.mesaGetter.isNotEmpty() && esInputValido(mnsj)) {
                WebSocketManager.send(mnsj, "chat")
                appendMensajePropio("Yo: $mnsj")
                messageInput.setText("")
            }
        }

        sendButton.isEnabled = false

        return valor
    }

    private fun reproducirSonidoAlerta() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun esMensajeSistema(mensaje: String): Boolean {
        return mensaje.contains("conectando", ignoreCase = true) ||
                mensaje.contains("Bienvenidos", ignoreCase = true) ||
                mensaje.contains("cerrando", ignoreCase = true) ||
                mensaje.contains("cerrado", ignoreCase = true) ||
                mensaje.contains("failure", ignoreCase = true) ||
                mensaje.contains("[error]", ignoreCase = true)
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

    private fun appendMensajeConColor(mensaje: String) {
        val spannable = SpannableString(mensaje + "\n")

        when {
            // mensajes propios
            mensaje.startsWith("PROPIO:") -> {
                spannable.setSpan(
                    ForegroundColorSpan(colorPropio),
                    0,
                    mensaje.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // mensajes de restaurante
            mensaje.startsWith("OTROS:") -> {
                spannable.setSpan(
                    ForegroundColorSpan(colorRestaurante),
                    0,
                    mensaje.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // mensajes de error
            mensaje.contains("[error]", ignoreCase = true) -> {
                spannable.setSpan(
                    ForegroundColorSpan(colorError),
                    0,
                    mensaje.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // mensajes de sistema
            mensaje.contains("conectando", ignoreCase = true) ||
                    mensaje.contains("Bienvenidos", ignoreCase = true) ||
                    mensaje.contains("cerrando", ignoreCase = true) ||
                    mensaje.contains("cerrado", ignoreCase = true) ||
                    mensaje.contains("failure", ignoreCase = true) -> {
                spannable.setSpan(
                    ForegroundColorSpan(colorSistema),
                    0,
                    mensaje.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // por defecto
            else -> {
                spannable.setSpan(
                    ForegroundColorSpan(colorRestaurante),
                    0,
                    mensaje.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // limpiar mensaje
        val mensajeLimpio = when {
            mensaje.startsWith("PROPIO:") -> mensaje.removePrefix("PROPIO:")
            mensaje.startsWith("OTROS:") -> mensaje.removePrefix("OTROS:")
            else -> mensaje
        }

        val spannableLimpio = SpannableString(mensajeLimpio + "\n")

        // aplicar color
        spannableLimpio.setSpan(
            ForegroundColorSpan(getColorParaMessage(mensaje)),
            0,
            mensajeLimpio.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        messagesView.append(spannableLimpio)
        // scroll
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun appendMensajeSistema(mensaje: String) {
        val spannable = SpannableString(mensaje + "\n")
        spannable.setSpan(
            ForegroundColorSpan(colorSistema),
            0,
            mensaje.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        messagesView.append(spannable)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun appendMensajePropio(mensaje: String) {
        val mensajeConPrefijo = "$mensaje"
        val spannable = SpannableString(mensajeConPrefijo + "\n")
        spannable.setSpan(
            ForegroundColorSpan(colorPropio),
            0,
            mensajeConPrefijo.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        messagesView.append(spannable)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun getColorParaMessage(mensaje: String): Int {
        return when {
            mensaje.startsWith("PROPIO:") -> colorPropio
            mensaje.startsWith("OTROS:") -> colorRestaurante
            mensaje.contains("[error]", ignoreCase = true) -> colorError
            else -> colorSistema
        }
    }

    private fun esMensajeDeChatValido(mensaje: String): Boolean {
        return mensaje.startsWith("OTROS:") &&
                !mensaje.contains("Cliente conectado", ignoreCase = true) &&
                !mensaje.contains("Cliente desconectado", ignoreCase = true) &&
                !mensaje.contains("----------------------------------------------------------", ignoreCase = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

    // obsoleto, pero tampoco come pan ahi
//    private fun appendMensaje(newMsg: String) {
//        messagesView.append(newMsg + "\n")
//        // scroll
//        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
//    }
