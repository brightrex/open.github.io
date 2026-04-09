package com.auralink.app.network

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class ChatServer(
    private val port: Int,
    private val onMessageReceived: (senderName: String, content: String) -> Unit
) : Thread() {

    companion object {
        const val TAG = "ChatServer"
    }

    private var serverSocket: ServerSocket? = null
    private val clients = mutableListOf<Socket>()

    @Volatile
    var isRunning = false

    override fun run() {
        isRunning = true
        try {
            serverSocket = ServerSocket(port)
            Log.d(TAG, "Server started on port $port")
            while (isRunning) {
                val client = serverSocket?.accept() ?: break
                Log.d(TAG, "Client connected: ${client.inetAddress.hostAddress}")
                synchronized(clients) { clients.add(client) }
                handleClient(client)
            }
        } catch (e: Exception) {
            if (isRunning) Log.e(TAG, "Server error: ${e.message}")
        }
    }

    private fun handleClient(socket: Socket) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (!socket.isClosed) {
                    val line = reader.readLine() ?: break
                    try {
                        val json = JSONObject(line)
                        val sender = json.getString("sender")
                        val content = json.getString("content")
                        onMessageReceived(sender, content)
                    } catch (e: Exception) {
                        Log.e(TAG, "Parse error: $line")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Client handler: ${e.message}")
            } finally {
                synchronized(clients) { clients.remove(socket) }
                try { socket.close() } catch (e: Exception) { /* ignore */ }
            }
        }.start()
    }

    fun stopServer() {
        isRunning = false
        synchronized(clients) {
            clients.forEach { try { it.close() } catch (e: Exception) { /* ignore */ } }
            clients.clear()
        }
        try { serverSocket?.close() } catch (e: Exception) { /* ignore */ }
    }
}
