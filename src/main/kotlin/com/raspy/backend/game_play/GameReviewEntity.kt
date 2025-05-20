package com.raspy.backend.game_play

import com.raspy.backend.game.GameEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_review")
data class GameReviewEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    val game: GameEntity,

    @Column(nullable = false)
    val manner: Int,

    @Column(nullable = false)
    val performance: Int,

    @Column(length = 1000)
    val text: String,

    val createdAt: LocalDateTime = LocalDateTime.now()
)