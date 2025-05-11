package com.raspy.backend.web_socket

import java.time.LocalDateTime

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: LocalDateTime,
)
