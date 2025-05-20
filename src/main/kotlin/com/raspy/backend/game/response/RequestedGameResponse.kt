package com.raspy.backend.game.response

import com.raspy.backend.game.enumerated.GameStatus
import com.raspy.backend.game.enumerated.ParticipationStatus
import java.time.LocalDateTime

data class RequestedGameResponse(
    val id: Long,
    val title: String,
    val description: String,
    val majorCategory: String,
    val minorCategory: String,
    val matchDate: LocalDateTime?,
    val matchLocation: String?,
    val gameStatus: GameStatus,

    val status: ParticipationStatus, // REQUESTED, APPROVED, REJECTED

    val ownerNickname: String,
    val ownerProfileUrl: String?,
    val ownerWins: Int,
    val ownerLosses: Int,
    val ownerDraws: Int,
    val ownerRating: Double
)
