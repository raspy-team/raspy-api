package com.raspy.backend.chat

import java.time.LocalDateTime

data class ChatJoinMessage(
    val userId: Long,
    val nickname: String,
    val joinedAt: LocalDateTime
)