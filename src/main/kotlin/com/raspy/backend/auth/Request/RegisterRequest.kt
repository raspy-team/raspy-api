package com.raspy.backend.auth.Request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    @field:Size(max = 50)
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 20)
    val password: String,

    @field:NotBlank
    @field:Size(min = 2, max = 30)
    val nickname: String,
)