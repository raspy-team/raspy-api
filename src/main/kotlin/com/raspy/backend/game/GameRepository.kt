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

    @Query("""
    SELECT g FROM GameEntity g
    JOIN FETCH g.rule
    JOIN FETCH g.createdBy cb
    LEFT JOIN FETCH cb.profile
    LEFT JOIN FETCH g.participations p
    WHERE g.id=:id
    """)
    fun findWithRuleAndCreatedByById(id: Long) : GameEntity?
}
