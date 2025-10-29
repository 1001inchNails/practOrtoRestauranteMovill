package com.example.practortorestaurantemovill.ui


import android.os.Bundle
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)
        messagesView = v.findViewById(R.id.messagesView)
        messageInput = v.findViewById(R.id.messageInput)
        sendButton = v.findViewById(R.id.sendButton)
        scrollView = v.findViewById(R.id.scrollView)

        WebSocketManager.incomingMessages.observe(viewLifecycleOwner, Observer { msg ->
            appendMessage(msg)
        })

        WebSocketManager.connectionStatus.observe(viewLifecycleOwner, Observer { status ->
            appendMessage("[status] $status")
        })

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            val sender = WebSocketManager.mesaGetter
            if (text.isNotEmpty()) {
                val json = JSONObject().apply {
                    put("sender", sender)
                    put("message", text)
                    put("timestamp", System.currentTimeMillis())
                }
                WebSocketManager.send(json.toString())
                appendMessage("Yo: $text")
                messageInput.setText("")
            }
        }

        return v
    }

    private fun appendMessage(newMsg: String) {
        messagesView.append(newMsg + "\n")
        // scroll
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}
