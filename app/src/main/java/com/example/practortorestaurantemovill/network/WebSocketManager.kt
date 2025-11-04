package com.example.practortorestaurantemovill.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebSocketManager {

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var url: String? = null

    private val _errors = MutableLiveData<String>()
    val errors: LiveData<String> = _errors

    private var mesa: String = "";

    var mesaSetter: String
        get() = mesa
        set(value) {
            if (!value.isBlank()) {
                mesa = value
            } else {
                throw IllegalArgumentException("Shit's fucked, yo")
            }
        }
    val mesaGetter: String
        get() = mesa

    // liveData para mensajes y estados
    private val _mensajesEntrantes = MutableLiveData<String>()
    val mensajesEntrantes: LiveData<String> = _mensajesEntrantes

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _systemEvents = MutableLiveData<String>()
    val systemEvents: LiveData<String> = _systemEvents

    private val _pedidoConfirmado = MutableLiveData<Boolean>()
    val pedidoConfirmado: LiveData<Boolean> = _pedidoConfirmado

    init {
        _pedidoConfirmado.value = false
    }

    // conexion y acciones
    fun connect(wsUrl: String) {
        if (webSocket != null) return
        url = wsUrl

        client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        _connectionStatus.postValue("conectando...")
        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                _connectionStatus.postValue("Bienvenidos/as")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                parsearAndPostearMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                parsearAndPostearMessage(bytes.utf8())
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(code, reason)
                _connectionStatus.postValue("cerrando")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectionStatus.postValue("cerrado")
                webSocket = null
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.postValue("failure: ${t.localizedMessage}")
                webSocket = null
            }
        })
    }

    // para enviar mensajes
    fun send(message: String, messageType: String = "") {
        val jsonMessage = JSONObject()

        if (messageType.isNotEmpty()) {
            jsonMessage.put("type", messageType)
        }

        jsonMessage.put("message", message)
        jsonMessage.put("sender", WebSocketManager.mesaGetter)
        jsonMessage.put("destino", "Restaurante")
        jsonMessage.put("timestamp", System.currentTimeMillis())
        jsonMessage.put("esPropio", true)

        webSocket?.let {
            it.send(jsonMessage.toString())
        } ?: run {
            _mensajesEntrantes.postValue("[error] no conectado")
        }
    }




    // procesamiento de mensajes entrantes
    private fun parsearAndPostearMessage(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val messageType = jsonObject.optString("type", "")
            System.out.println("messageType::: "+messageType)

            when (messageType) {
                "error" -> {
                    val errorCode = jsonObject.optString("errorCode", "")
                    val errorMessage = jsonObject.optString("message", "")
                    _errors.postValue("Error $errorCode: $errorMessage")
                }
                "success" -> {
                    val status = jsonObject.optString("status", "")
                    val message = jsonObject.optString("message", "")
                    _mensajesEntrantes.postValue("$status")
                }
                "pedido_enviado_a_mesa" -> {
                    _systemEvents.postValue("pedido_enviado_a_mesa")
                    _pedidoConfirmado.postValue(true)
                }
                "pedido_cancelado_a_mesa" -> {
                    _systemEvents.postValue("pedido_cancelado_a_mesa")
                }
                "client_connect" -> {
                    val message = jsonObject.optString("message", "")
                    //_mensajesEntrantes.postValue("$message")
                }
                "client_disconnect" -> {
                    val message = jsonObject.optString("message", "")
                    //_mensajesEntrantes.postValue("$message")
                }
                "chat" -> {
                    val sender = jsonObject.optString("sender", "Unknown")
                    val destino = jsonObject.optString("destino", "Unknown")
                    val messageContent = jsonObject.optString("message", "")
                    val esPropio = jsonObject.optBoolean("esPropio", false)

                    System.out.println("destino: $destino   mesa: $mesa")

                    if (destino == mesa) {
                        val formattedMessage = if (esPropio) {
                            "PROPIO:$sender: $messageContent"
                        } else {
                            "OTROS:$sender: $messageContent"
                        }
                        _mensajesEntrantes.postValue(formattedMessage)
                    }
                }
            }

        } catch (e: Exception) {
            _mensajesEntrantes.postValue("[Error de parseo de mensaje]: $jsonString")
        }
    }

    // para mensaje de envio de pedido
    fun sendPedidoMensaje(message: String) {
        val jsonMessage = JSONObject().apply {
            put("type", "pedido")
            put("sender", mesaGetter)
            put("destino", "Restaurante")
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }

        webSocket?.let {
            it.send(jsonMessage.toString())
            System.out.println("Pedido enviado: $jsonMessage")
        } ?: run {
            _mensajesEntrantes.postValue("[error] no conectado")
        }
    }

    // reseteo de estados necesarios para la botonera y los checkboxes
    fun resetStates() {
        _connectionStatus.value = ""
        _pedidoConfirmado.value = false
        _systemEvents.value = ""
        _mensajesEntrantes.value = ""
        _errors.value = ""
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
        client?.dispatcher?.executorService?.shutdown()
        client = null
        resetStates()
    }
}