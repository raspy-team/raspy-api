package com.raspy.backend.web_socket

import com.raspy.backend.chat.MessageType
import java.time.LocalDateTime

data class ChatMessage(
    val senderId: Long,
    val sender: String,
    val content: String,
    val timestamp: LocalDateTime,
    val messageType: MessageType
)
