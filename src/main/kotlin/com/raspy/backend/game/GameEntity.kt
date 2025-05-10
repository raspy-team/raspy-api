package com.raspy.backend.game

import com.raspy.backend.game.enumerated.WinCondition
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "custom_game")
data class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    val title: String,

    @Column(nullable = false, length = 255)
    val description: String,

    @Column(nullable = false, length = 500)
    val rulesDescription: String, // 규칙 설명

    @Column(nullable = false, length = 50)
    val majorCategory: String, // 대분류

    @Column(nullable = false, length = 50)
    val minorCategory: String, // 소분류

    @Column(nullable = false)
    val pointsToWin: Int, // points_to_win (무제한일 경우 -1)

    @Column(nullable = false)
    val setsToWin: Int, // sets_to_win

    @Column(nullable = false)
    val duration: Int, // duration (초 단위, 무제한은 -1)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val winBy: WinCondition, // winBy: 세트 반 이상 승리 / 제한시간 동안 많은 세트 및 점수 획득 시 승리

    @Column(nullable = true)
    val matchDate: LocalDateTime?, // 경기 일시 (협의 가능일 경우 null)

    @Column(nullable = true)
    val placeRoad: String?, // 장소 도로명주소 (협의 가능일 경우 null)

    @Column(nullable = true)
    val placeDetail: String?, // 장소 상세주소 (협의 가능일 경우 null)

    @ManyToMany
    @JoinTable(
        name = "game_participants",
        joinColumns = [JoinColumn(name = "game_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val participants: Set<UserEntity> = emptySet(),

    @Column(nullable = false)
    val maxPlayers: Int = 2,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity, // 생성한 유저

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 생성일시

    @Column(nullable = false)
    val modifiedAt: LocalDateTime = LocalDateTime.now() // 수정일시
)
