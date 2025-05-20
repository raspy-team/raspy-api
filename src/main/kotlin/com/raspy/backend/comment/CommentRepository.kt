package com.raspy.backend.comment

import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<CommentEntity, Long>{
    fun findByGameIdOrderByCreatedAtDesc(gameId: Long): List<CommentEntity>

}