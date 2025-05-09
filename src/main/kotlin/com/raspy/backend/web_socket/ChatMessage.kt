package com.raspy.backend.web_socket

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: Long
)
