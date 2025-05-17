package com.raspy.backend.game

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RuleDto (
    @field:NotNull
    @field:Size(min = 1, max = 500)
    val ruleDescription: String, // 규칙 설명

    @field:Min(value = -1)
    val pointsToWin: Int, // points_to_win (무제한일 경우 -1)

    @field:Min(value = 1)
    val setsToWin: Int, // sets_to_win (최소 1)

    @field:Min(value = -1)
    val duration: Int, // duration (초 단위, 무제한은 -1)

    @field:NotNull
    val winBy: String, // "SETS_HALF_WIN" or "MOST_SETS_AND_POINTS"

)