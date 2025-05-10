package com.raspy.backend.game.response

import java.time.LocalDateTime

data class GameSummaryResponse(
    val id: Long,
    val title: String,
    val majorCategory: String,
    val minorCategory: String,
    val description: String,
    val currentParticipantCounts: Int,
    val maxPlayers: Int,
    val matchDate: LocalDateTime?,
    val matchLocation: String?
)