package com.raspy.backend.game_play.request

data class ReviewRequest(
    val manner: Int,
    val performance: Int,
    val text: String?
)