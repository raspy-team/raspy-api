package com.raspy.backend.game

import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "custom_game")
class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    val title: String,

    @Column(nullable = false, length = 255)
    val description: String,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", referencedColumnName = "id")
    val rule: RuleEntity,

    @Column(nullable = true)
    val matchDate: LocalDateTime?,

    @Column(nullable = true)
    val placeRoad: String?,

    @Column(nullable = true)
    val placeDetail: String?,

    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], orphanRemoval = true)
    val participations: MutableSet<ParticipationEntity> = mutableSetOf(),

    @Column(nullable = false)
    val maxPlayers: Int = 2,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 현재 참가 중인 유저 목록 조회
     */
    fun getParticipants(): Set<UserEntity> = participations.map { it.user }.toSet()
}
