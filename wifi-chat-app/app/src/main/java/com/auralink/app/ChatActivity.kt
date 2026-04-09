package com.auralink.app

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.auralink.app.adapter.MessageAdapter
import com.auralink.app.databinding.ActivityChatBinding
import com.auralink.app.model.Message
import com.auralink.app.network.ChatClient

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatClient: ChatClient
    private lateinit var myUsername: String
    private lateinit var peerDisplayName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        myUsername = intent.getStringExtra(MainActivity.EXTRA_USERNAME) ?: "Me"
        val peerName = intent.getStringExtra(DiscoveryActivity.EXTRA_DEVICE_NAME) ?: "Unknown"
        val deviceHost = intent.getStringExtra(DiscoveryActivity.EXTRA_DEVICE_HOST) ?: ""
        val devicePort = intent.getIntExtra(
            DiscoveryActivity.EXTRA_DEVICE_PORT, MainActivity.SERVER_PORT
        )

        peerDisplayName = peerName.removePrefix("AuraLink_")
        binding.tvPeerName.text = peerDisplayName
        binding.tvPeerInitial.text = peerDisplayName.firstOrNull()?.uppercase() ?: "?"

        // RecyclerView
        messageAdapter = MessageAdapter(mutableListOf())
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        // Route incoming messages from the app-level server to this screen
        (application as AuraLinkApp).messageListener = { senderName, content ->
            runOnUiThread { addReceivedMessage(senderName, content) }
        }

        // Connect as client to the selected peer
        chatClient = ChatClient(
            host = deviceHost,
            port = devicePort,
            onMessageReceived = { senderName, content ->
                runOnUiThread { addReceivedMessage(senderName, content) }
            },
            onConnected = {
                runOnUiThread {
                    binding.tvConnectionStatus.text = "● Connected"
                    binding.tvConnectionStatus.setTextColor(getColor(R.color.status_online))
                }
            },
            onDisconnected = {
                runOnUiThread {
                    binding.tvConnectionStatus.text = "● Disconnected"
                    binding.tvConnectionStatus.setTextColor(getColor(R.color.text_secondary))
                }
            }
        )
        chatClient.connect()

        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isEmpty()) return@setOnClickListener
            binding.etMessage.setText("")
            val message = Message(content = content, senderName = myUsername, isSentByMe = true)
            messageAdapter.addMessage(message)
            binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
            chatClient.sendMessage(myUsername, content)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun addReceivedMessage(senderName: String, content: String) {
        val message = Message(content = content, senderName = senderName, isSentByMe = false)
        messageAdapter.addMessage(message)
        binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as AuraLinkApp).messageListener = null
        chatClient.disconnect()
    }
}
