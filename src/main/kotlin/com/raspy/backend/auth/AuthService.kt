package com.raspy.backend.auth

import com.raspy.backend.auth.Request.RegisterRequest
import com.raspy.backend.exception.DuplicateEmailException
import com.raspy.backend.exception.InvalidCredentialsException
import com.raspy.backend.exception.UserNotFoundException
import com.raspy.backend.jwt.JwtUtil
import com.raspy.backend.jwt.UserPrincipal
import com.raspy.backend.user.enumerated.Role
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    fun register(req: RegisterRequest) {
        log.info { "Attempting to register user: ${req.email}" }

        if (userRepository.findByEmail(req.email).isPresent) {
            log.warn { "Registration failed - email already exists: ${req.email}" }
            throw DuplicateEmailException("Email already exists")
        }

        val user = UserEntity(
            email = req.email,
            password = passwordEncoder.encode(req.password),
            nickname = req.nickname,
            roles = setOf(Role.ROLE_USER)
        )

        userRepository.save(user)
        log.info { "User registered successfully: ${req.email}" }
    }

    fun login(email: String, password: String): String {
        log.info { "Login attempt for email: $email" }

        val user = userRepository.findByEmail(email)
            .orElseThrow {
                log.warn { "Login failed - user not found: $email" }
                UserNotFoundException("User not found")
            }

        if (!passwordEncoder.matches(password, user.password)) {
            log.warn { "Login failed - invalid credentials for email: $email" }
            throw InvalidCredentialsException("Invalid credentials")
        }

        val roles = user.roles.map { it.name }
        val token = jwtUtil.generateToken(user.id!!, user.email, roles)

        log.info { "Login successful - JWT issued for email: $email" }
        return token
    }

    fun getCurrentUser(): UserPrincipal {
        val auth = SecurityContextHolder.getContext().authentication
            ?: run {
                log.error { "No authentication found in security context" }
                throw IllegalStateException("No authentication found")
            }

        val principal = auth.principal as? UserPrincipal
            ?: run {
                log.error { "Invalid principal type in security context" }
                throw IllegalStateException("Invalid principal type")
            }

        log.debug { "Authenticated user retrieved: ${principal.email}" }
        return principal
    }
}
