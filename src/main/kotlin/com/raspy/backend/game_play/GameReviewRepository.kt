package com.raspy.backend.game_play

import org.springframework.data.jpa.repository.JpaRepository

interface GameReviewRepository : JpaRepository<GameReviewEntity, Long> {
    fun findAllByGameId(gameId: Long): List<GameReviewEntity>
}