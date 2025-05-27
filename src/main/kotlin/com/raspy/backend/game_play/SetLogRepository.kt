package com.raspy.backend.game_play

import org.springframework.data.jpa.repository.JpaRepository

interface SetLogRepository: JpaRepository<SetLogEntity, Long> {
    fun findByGameIdOrderByStartedAtDesc(gameId: Long): SetLogEntity
    fun findAllByGameId(gameId: Long): List<SetLogEntity>


}