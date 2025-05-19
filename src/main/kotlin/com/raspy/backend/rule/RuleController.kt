package com.raspy.backend.rule

import com.raspy.backend.auth.AuthService
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rules")
class RuleController(
    private val ruleService: RuleService,
    private val authService: AuthService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/list")
    @Operation(summary = "사용자 규칙 목록 조회", description = "현재 로그인한 사용자가 만든 규칙 목록을 반환합니다.")
    fun getMyRules(): ResponseEntity<List<RuleResponse>> {
        log.info { "규칙 목록 요청" }
        return ResponseEntity.ok(ruleService.getAllRules())
    }
}