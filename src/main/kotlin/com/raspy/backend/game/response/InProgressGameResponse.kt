package com.raspy.backend.game.response

import java.time.LocalDateTime

data class InProgressGameResponse(
    val id: Long,
    val myNickname: String,
    val myProfileUrl: String?,
    val opponentNickname: String,
    val opponentProfileUrl: String?,
    val endsAt: LocalDateTime?
)