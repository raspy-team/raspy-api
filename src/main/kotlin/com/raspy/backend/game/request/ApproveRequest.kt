package com.raspy.backend.game.request

data class ApproveRequest(
    val gameId: Long,
    val userId: Long
)
