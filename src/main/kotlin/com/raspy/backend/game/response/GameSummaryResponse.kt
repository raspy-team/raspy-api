package com.raspy.backend.game.response

import java.time.LocalDateTime

data class GameSummaryResponse(
    val id: Long,
    val majorCategory: String,
    val minorCategory: String,
    val currentParticipantCounts: Int,
    val maxPlayers: Int,
    val matchDate: LocalDateTime?,
    val matchLocation: String?,

    val createdAt: LocalDateTime,
    val ownerNickname: String,
    val ownerProfileUrl: String?,
    val ruleTitle: String,
    val ruleDescription: String,
    val winCondition: String,
    val points: Int,
    val sets: Int,
    val duration: Int
)