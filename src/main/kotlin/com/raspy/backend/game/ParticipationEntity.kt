package com.raspy.backend.game

import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_participation")
class ParticipationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    val game: GameEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ParticipationStatus = ParticipationStatus.REQUESTED,

    @Column(nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var leftAt: LocalDateTime? = null
)