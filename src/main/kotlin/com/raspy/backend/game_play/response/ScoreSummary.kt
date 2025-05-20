package com.raspy.backend.game_play.response

data class ScoreSummary(
    val nickname: String,
    val profileUrl: String?,
    val sets: Int
)
