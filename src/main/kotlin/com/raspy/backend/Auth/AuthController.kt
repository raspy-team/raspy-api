package com.raspy.backend.Auth

import com.raspy.backend.Auth.Request.LoginRequest
import com.raspy.backend.Auth.Request.RegisterRequest
import com.raspy.backend.Auth.Response.LoginResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "사용자 회원가입",
        description = "회원가입 요청을 처리합니다. 클라이언트에서 이메일, 비밀번호, 닉네임을 보내면 신규 사용자를 생성합니다.\n" +
                "이메일은 중복 불가하며, 비밀번호는 최소 8자 이상이어야 합니다. 성공 시 201 Created와 성공 메시지를 반환합니다."
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<String> {
        authService.register(req)
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully")
    }

    @Operation(
        summary = "사용자 로그인",
        description = "로그인 요청을 처리합니다. 클라이언트에서 이메일과 비밀번호를 보내면 검증 후 JWT 토큰을 발급합니다.\n" +
                "토큰은 Authorization 헤더에 Bearer 방식으로 포함하여 이후 요청에 사용됩니다. 성공 시 200 OK와 JWT 토큰을 반환합니다."
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        val token = authService.login(req.email, req.password)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
