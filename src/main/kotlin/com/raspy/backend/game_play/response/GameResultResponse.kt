package com.raspy.backend.game_play.response

import java.time.LocalDateTime

data class GameResultResponse(
    val id: Long,
    val user1: ScoreSummary,
    val user2: ScoreSummary,
    val winner: UserSummary,
    val place: String,
    val majorCategory: String,
    val minorCategory: String,
    val ruleTitle: String,
    val matchDate: LocalDateTime
)
