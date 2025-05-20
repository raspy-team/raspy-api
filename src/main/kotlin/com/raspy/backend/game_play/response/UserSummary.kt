package com.raspy.backend.game_play.response



data class UserSummary(
    val id: Long,
    val nickname: String,
    val profileUrl: String?
)