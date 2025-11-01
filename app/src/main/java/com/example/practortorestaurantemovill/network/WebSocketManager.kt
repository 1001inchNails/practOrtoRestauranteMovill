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

    // LiveData for incoming messages and status
    private val _incomingMessages = MutableLiveData<String>()
    val incomingMessages: LiveData<String> = _incomingMessages

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    fun connect(wsUrl: String) {
        if (webSocket != null) return // already connected or connecting
        url = wsUrl

        client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        _connectionStatus.postValue("connecting")
        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                _connectionStatus.postValue("open")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                parseAndPostMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                parseAndPostMessage(bytes.utf8())
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(code, reason)
                _connectionStatus.postValue("closing")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _connectionStatus.postValue("closed")
                webSocket = null
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.postValue("failure: ${t.localizedMessage}")
                webSocket = null
            }
        })
    }

    fun send(message: String, messageType: String = "") {
        val jsonMessage = JSONObject()

        if (messageType.isNotEmpty()) {
            jsonMessage.put("type", messageType)
        }

        jsonMessage.put("message", message)
        jsonMessage.put("sender", WebSocketManager.mesaGetter)
        jsonMessage.put("destino", "Restaurante")
        jsonMessage.put("timestamp", System.currentTimeMillis())

        webSocket?.let {
            it.send(jsonMessage.toString())
        } ?: run {
            _incomingMessages.postValue("[error] not connected")
        }
    }

    private fun parseAndPostMessage(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            val messageType = jsonObject.optString("type", "")
            val sender = jsonObject.optString("sender", "Unknown")
            val messageContent = jsonObject.optString("message", "")

            if (messageType == "chat"){
                var formattedMessage = "$sender: "
                formattedMessage += messageContent
                _incomingMessages.postValue(formattedMessage)
            }


        } catch (e: Exception) {
            _incomingMessages.postValue("[Error parsing message]: $jsonString")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}