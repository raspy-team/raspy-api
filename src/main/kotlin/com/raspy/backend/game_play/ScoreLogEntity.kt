package com.raspy.backend.game_play

import com.raspy.backend.game.GameEntity
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class ScoreLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne @JoinColumn(name = "game_id")
    val game: GameEntity,

    @ManyToOne @JoinColumn(name = "actor_id")
    val actor: UserEntity,

    @ManyToOne @JoinColumn(name = "target_id")
    val target: UserEntity,

    @Column(nullable = false)
    val action: String,  // "INCREMENT" or "DECREMENT"

    val point: Int = 1,
    val setIndex: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)