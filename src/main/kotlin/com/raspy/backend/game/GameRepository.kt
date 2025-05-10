package com.raspy.backend.game

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : JpaRepository<GameEntity, Long> {
    @EntityGraph(attributePaths = ["rule", "participants"])
    @Query("SELECT g FROM GameEntity g")
    fun findAllAtGameList(): List<GameEntity>
}
