package com.raspy.backend.chat

import java.time.LocalDateTime

data class ChatStatusChangeMessage(
    val sender: String,
    val content: String,
    val timestamp: LocalDateTime=LocalDateTime.now(),
)