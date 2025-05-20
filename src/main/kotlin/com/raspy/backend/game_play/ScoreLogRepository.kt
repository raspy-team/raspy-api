package com.raspy.backend.game_play


import org.springframework.data.jpa.repository.JpaRepository

interface ScoreLogRepository : JpaRepository<ScoreLogEntity, Long> {
    fun findAllByGameIdOrderByTimestampAsc(gameId: Long): List<ScoreLogEntity>
}