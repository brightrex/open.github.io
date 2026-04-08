package com.auralink.app.network

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ChatClient(
    private val host: String,
    private val port: Int,
    private val onMessageReceived: (senderName: String, content: String) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit
) {
    companion object {
        const val TAG = "ChatClient"
    }

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    @Volatile
    private var isConnected = false

    fun connect() {
        Thread {
            try {
                socket = Socket(host, port)
                writer = PrintWriter(socket!!.getOutputStream(), true)
                isConnected = true
                Log.d(TAG, "Connected to $host:$port")
                onConnected()

                val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                while (isConnected) {
                    val line = reader.readLine() ?: break
                    try {
                        val json = JSONObject(line)
                        onMessageReceived(json.getString("sender"), json.getString("content"))
                    } catch (e: Exception) {
                        Log.w(TAG, "Parse: $line")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error: ${e.message}")
            } finally {
                isConnected = false
                onDisconnected()
                disconnect()
            }
        }.start()
    }

    fun sendMessage(senderName: String, content: String) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("sender", senderName)
                    put("content", content)
                    put("timestamp", System.currentTimeMillis())
                }
                writer?.println(json.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Send error: ${e.message}")
            }
        }.start()
    }

    fun disconnect() {
        isConnected = false
        try { socket?.close() } catch (e: Exception) { /* ignore */ }
        socket = null
        writer = null
    }
}
