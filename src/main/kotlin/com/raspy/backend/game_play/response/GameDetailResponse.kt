package com.raspy.backend.game_play.response

import com.raspy.backend.game.enumerated.WinCondition
import java.time.LocalDateTime

data class GameDetailResponse(
    val id: Long,
    val majorCategory: String,
    val minorCategory: String,
    val ruleTitle: String,
    val place: String,
    val matchDate: LocalDateTime?,
    val user1: UserSummary,
    val user2: UserSummary,
    val score1: Int,
    val score2: Int,
    val setsToWin: Int,
    val pointsToWin: Int,
    val winBy: WinCondition,
    val currentSet: Int,
    val limitSeconds: Int,
    // TODO : 세트마다 시간 잴건지? 그럼 이게 달라짐.
    val startedAt: LocalDateTime,
    val chatRoomId: String // UUID 임.

)
