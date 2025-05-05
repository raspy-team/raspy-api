package com.raspy.backend.auth.Request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)