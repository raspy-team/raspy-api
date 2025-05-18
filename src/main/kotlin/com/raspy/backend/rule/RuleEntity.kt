package com.raspy.backend.rule

import com.raspy.backend.game.enumerated.WinCondition
import jakarta.persistence.*


@Entity
@Table(name = "game_rule")
data class RuleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * fill with using gpt AI
     */
    @Column(nullable = false, length = 200)
    var ruleTitle: String,

    @Column(nullable = false, length = 500)
    var ruleDescription: String,

    @Column(nullable = false, length = 50)
    var majorCategory: String,

    @Column(nullable = false, length = 50)
    var minorCategory: String,

    @Column(nullable = false)
    var pointsToWin: Int,

    @Column(nullable = false)
    var setsToWin: Int,

    @Column(nullable = false)
    var duration: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var winBy: WinCondition,

    @Column(nullable = false, length = 200)
    val createdBy: String,

    @Column(nullable = false)
    val useCount: Long = 0  // 룰이 사용된 횟수
)
