    package com.raspy.backend.game.response

    import com.raspy.backend.game.enumerated.GameStatus
    import java.time.LocalDateTime

    data class MyGameResponse(
        val id: Long,
        val matchLocation: String?,
        val matchDate: LocalDateTime?,

        val majorCategory: String,
        val minorCategory: String,
        val ruleTitle: String,
        val ruleDescription: String,

        val isOwner: Boolean,

        val myNickname: String,
        val myProfileUrl: String?,

        val opponentId: Long,
        val opponentNickname: String,
        val opponentProfileUrl: String?,
        val opponentWins: Int,
        val opponentLosses: Int,
        val opponentDraws: Int,
        val opponentRating: Double,

        val status: GameStatus
    )
