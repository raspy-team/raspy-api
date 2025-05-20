package com.raspy.backend.game_play.response

import java.time.LocalDateTime

data class GameDetailResponse(
    val id: Long,
    val majorCategory: String,
    val minorCategory: String,
    val ruleTitle: String,
    val place: String,
    val matchDate: LocalDateTime,
    val user1: UserSummary,
    val user2: UserSummary,
    val score1: Int,
    val score2: Int,
    val currentSet: Int,
    val limitSeconds: Int,
    val startedAt: LocalDateTime
)
