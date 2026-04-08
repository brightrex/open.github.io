package com.auralink.app

import android.app.Application
import com.auralink.app.network.ChatServer

class AuraLinkApp : Application() {

    /**
     * A single TCP server that runs for the lifetime of the app.
     * All incoming messages are routed to the registered [messageListener].
     * ChatActivity sets this listener when active and clears it on destroy.
     */
    lateinit var chatServer: ChatServer
        private set

    /** Registered by ChatActivity to display incoming messages. */
    var messageListener: ((senderName: String, content: String) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        chatServer = ChatServer(MainActivity.SERVER_PORT) { sender, content ->
            messageListener?.invoke(sender, content)
        }
        chatServer.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        chatServer.stopServer()
    }
}
