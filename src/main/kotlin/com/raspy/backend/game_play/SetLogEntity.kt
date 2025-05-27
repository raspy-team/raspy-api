package com.raspy.backend.game_play

import com.raspy.backend.game.GameEntity
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * set을 시작하는 기록을 남기는 것임
 */
@Entity
class SetLogEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "game_id")
    val game: GameEntity,

    @ManyToOne
    @JoinColumn(name = "actor_id")
    val actor: UserEntity,

    val totalSetIndex: Int,
    val startedAt: LocalDateTime = LocalDateTime.now()
)