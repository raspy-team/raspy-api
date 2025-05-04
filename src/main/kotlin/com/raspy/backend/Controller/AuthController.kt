package com.raspy.backend.Controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @Operation(
        summary = "test",
        description = "auth test",
    )
    @PostMapping("/auth-test")
    fun register(@RequestBody request: String): String = "t"+"t"

}

