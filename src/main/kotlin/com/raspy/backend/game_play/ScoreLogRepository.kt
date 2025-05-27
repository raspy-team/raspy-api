package com.raspy.backend.game_play


import com.raspy.backend.game.GameEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ScoreLogRepository : JpaRepository<ScoreLogEntity, Long> {
    fun findAllByGameIdOrderByTimestampAsc(gameId: Long): List<ScoreLogEntity>

    fun findAllByGame(game: GameEntity): List<ScoreLogEntity>
    fun findAllByGameAndSetIndex(game: GameEntity, setIndex: Int): List<ScoreLogEntity>
}