package com.auralink.app.model

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSentByMe: Boolean
)
