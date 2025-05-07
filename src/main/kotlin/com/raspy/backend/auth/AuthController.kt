package com.raspy.backend.auth

import com.raspy.backend.auth.Request.LoginRequest
import com.raspy.backend.auth.Request.RegisterRequest
import com.raspy.backend.auth.Response.LoginResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "사용자 회원가입",
        description = "이메일, 비밀번호, 닉네임을 받아 신규 사용자를 생성합니다.",
        responses = [
            ApiResponse(responseCode = "201", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
            ApiResponse(responseCode = "409", description = "중복된 이메일")
        ]
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<String> {
        log.info { "Received registration request for email: ${req.email}" }

        authService.register(req)

        log.info { "User registered successfully: ${req.email}" }
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully")
    }

    @Operation(
        summary = "사용자 로그인",
        description = "이메일과 비밀번호를 받아 로그인 후 JWT 토큰을 발급합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 반환"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
            ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
        ]
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        log.info { "Received login request for email: ${req.email}" }

        val token = authService.login(req.email, req.password)

        log.info { "Login successful for email: ${req.email}" }
        return ResponseEntity.ok(LoginResponse(token))
    }
}
