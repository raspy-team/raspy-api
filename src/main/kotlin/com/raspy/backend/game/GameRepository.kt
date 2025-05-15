package com.raspy.backend.game

import com.raspy.backend.user.UserEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : JpaRepository<GameEntity, Long> {
    @EntityGraph(attributePaths = ["rule", "participations"])
    @Query("SELECT g FROM GameEntity g order by g.createdAt desc")
    fun findAllAtGameList(): List<GameEntity>

    @Query("SELECT g FROM GameEntity g where g.createdBy = :user")
    fun findAllByCreatedBy(user: UserEntity): List<GameEntity>
}
