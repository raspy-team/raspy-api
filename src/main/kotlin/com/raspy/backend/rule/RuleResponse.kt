package com.raspy.backend.rule

import com.raspy.backend.game.enumerated.WinCondition

data class RuleResponse(
    val id: Long,
    val ruleTitle: String,
    val majorCategory: String,
    val minorCategory: String,
    val ruleDescription: String,
    val pointsToWin: Int,
    val setsToWin: Int,
    val duration: Int,
    val winBy: WinCondition
)