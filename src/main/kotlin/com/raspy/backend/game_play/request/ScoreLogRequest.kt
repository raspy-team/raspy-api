package com.raspy.backend.game_play.request

data class ScoreLogRequest(
    val targetId: Long,
    val action: String  // INCREMENT, DECREMENT
)