package com.raspy.backend.game

import com.raspy.backend.game.enumerated.GameStatus
import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ParticipationRepository : JpaRepository<ParticipationEntity, Long> {
    /**
     * 특정 게임과 유저에 대한 참여 기록 조회
     */
    fun findByGameAndUser(game: GameEntity, user: UserEntity): ParticipationEntity?
    fun existsByGameAndUser(game: GameEntity, user: UserEntity): Boolean
    fun findAllByUserAndStatus(user: UserEntity, status: ParticipationStatus): List<ParticipationEntity>
    fun findAllByGameOrderByIdDesc(game: GameEntity): List<ParticipationEntity>
    fun countByGameAndStatus(game: GameEntity, status: ParticipationStatus): Long
    fun findAllByUserOrderByIdDesc(userEntity: UserEntity): List<ParticipationEntity>
    fun findAllByUser(user: UserEntity): List<ParticipationEntity>

    @Query(
        """
    SELECT p FROM ParticipationEntity p 
    WHERE p.user = :user 
      AND p.status = :status 
      AND p.game.gameStatus = :gameStatus
"""
    )
    fun findCurrentLiveGame(
        @Param("user") user: UserEntity,
        @Param("status") status: ParticipationStatus = ParticipationStatus.APPROVED,
        @Param("gameStatus") gameStatus: GameStatus = GameStatus.IN_PROGRESS
    ): ParticipationEntity?

}