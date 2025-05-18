package com.raspy.backend.rule

import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<RuleEntity, Long> {
    fun findAllByCreatedBy(createdBy: String): List<RuleEntity>

}