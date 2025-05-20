package com.raspy.backend.game

import com.raspy.backend.game.enumerated.GameStatus
import com.raspy.backend.rule.RuleEntity
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "custom_game")
class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", referencedColumnName = "id")
    var rule: RuleEntity,

    @Column(nullable = true)
    var matchDate: LocalDateTime?,

    @Column(nullable = true)
    var placeRoad: String?,

    @Column(nullable = true)
    var placeDetail: String?,

    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participations: MutableSet<ParticipationEntity> = mutableSetOf(),

    @Column(nullable = false)
    var maxPlayers: Int = 2,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: UserEntity,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var gameStatus: GameStatus = GameStatus.MATCHING,
) {
    /**
     * 현재 참가 중인 유저 목록 조회
     */
    fun getParticipants(): Set<UserEntity> = participations.map { it.user }.toSet()
}
