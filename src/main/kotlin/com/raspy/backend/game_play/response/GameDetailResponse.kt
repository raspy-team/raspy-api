package com.raspy.backend.game_play.response

import com.raspy.backend.game.enumerated.WinCondition
import java.time.LocalDateTime

data class GameDetailResponse(
    val id: Long,
    val majorCategory: String,
    val minorCategory: String,
    val ruleTitle: String,
    val place: String,

    /**
     * match date와 실제 시작 시간은 다를 수 있음.
     */
    val matchDate: LocalDateTime?,

    val totalGameStartedAt: LocalDateTime? = null,
    // set started at
    val setStartedAt: LocalDateTime? = null,

    val user1: UserSummary,
    val user2: UserSummary,

    val score1: Int,
    val score2: Int,

    val set1: Int,
    val set2: Int,

    val setsToWin: Int,
    val pointsToWin: Int,
    val winBy: WinCondition,

    val limitSeconds: Int,

    val chatRoomId: String // UUID 임.

)
