package com.raspy.backend.game

import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipationRepository : JpaRepository<ParticipationEntity, Long> {
    /**
     * 특정 게임과 유저에 대한 참여 기록 조회
     */
    fun findByGameAndUser(game: GameEntity, user: UserEntity): ParticipationEntity?
    fun existsByGameAndUser(game: GameEntity, user: UserEntity): Boolean
    fun findAllByUserAndStatus(user: UserEntity, status: ParticipationStatus): List<ParticipationEntity>
    fun findAllByGameInAndStatus(games: List<GameEntity>, status: ParticipationStatus): List<ParticipationEntity>
    }