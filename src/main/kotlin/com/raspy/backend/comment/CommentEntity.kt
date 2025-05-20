package com.raspy.backend.comment

import com.raspy.backend.game.GameEntity
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_comment")
data class CommentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    val game: GameEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    val author: UserEntity,

    @Column(nullable = false, length = 1000)
    val content: String,

    val createdAt: LocalDateTime
)