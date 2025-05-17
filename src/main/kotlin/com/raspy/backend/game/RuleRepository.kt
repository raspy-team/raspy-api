package com.raspy.backend.game

import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<RuleEntity, Long> {
}