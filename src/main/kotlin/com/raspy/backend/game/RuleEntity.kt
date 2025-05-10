package com.raspy.backend.game

import com.raspy.backend.game.enumerated.WinCondition
import jakarta.persistence.*


@Entity
@Table(name = "game_rule")
data class RuleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 500)
    val rulesDescription: String,

    @Column(nullable = false, length = 50)
    val majorCategory: String,

    @Column(nullable = false, length = 50)
    val minorCategory: String,

    @Column(nullable = false)
    val pointsToWin: Int,

    @Column(nullable = false)
    val setsToWin: Int,

    @Column(nullable = false)
    val duration: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val winBy: WinCondition,

    @Column(nullable = false)
    val useCount: Long = 0  // 룰이 사용된 횟수
)
