package com.raspy.backend.game.response

import java.time.LocalDateTime

data class GameApplicantsResponse(
    val gameId: Long,
    val title: String,
    val description: String,
    val majorCategory: String,
    val minorCategory: String,
    val matchDate: LocalDateTime?,
    val matchLocation: String?,
    val applicants: List<ApplicantInfo>
)

data class ApplicantInfo(
    val userId: Long,
    val applicantNickname: String,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val rating: Double,
    val approved: Boolean
)