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

    private fun parseAndPostMessage(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val sender = jsonObject.optString("sender", "Unknown")
            val message = jsonObject.optString("message", "")

            // Format the message as "Sender: message"
            val formattedMessage = "$sender: $message"
            _incomingMessages.postValue(formattedMessage)

        } catch (e: Exception) {
            // If parsing fails, post the original message with error info
            _incomingMessages.postValue("[Error parsing message]: $jsonString")
        }
    }

    fun send(message: String) {
        webSocket?.let {
            it.send(message)
        } ?: run {
            _incomingMessages.postValue("[error] not connected")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}