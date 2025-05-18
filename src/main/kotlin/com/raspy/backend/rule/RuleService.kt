package com.raspy.backend.rule

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RuleService(
    private val ruleRepository: RuleRepository
) {
    private val log = KotlinLogging.logger {}

    fun getRulesByUser(email: String): List<RuleResponse> =
        ruleRepository.findAllByCreatedBy(email)
            .map { it.toResponse() }

    private fun RuleEntity.toResponse(): RuleResponse =
        RuleResponse(
            id = id,
            ruleTitle = ruleTitle,
            ruleDescription = ruleDescription,
            pointsToWin = pointsToWin,
            setsToWin = setsToWin,
            duration = duration,
            winBy = winBy,
            majorCategory = majorCategory,
            minorCategory = minorCategory,
        )
}