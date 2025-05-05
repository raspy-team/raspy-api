package com.raspy.backend.auth

import com.raspy.backend.auth.Request.RegisterRequest
import com.raspy.backend.exception.DuplicateEmailException
import com.raspy.backend.exception.InvalidCredentialsException
import com.raspy.backend.exception.UserNotFoundException
import com.raspy.backend.jwt.JwtUtil
import com.raspy.backend.user.Role
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {
    private val passwordEncoder = BCryptPasswordEncoder()


    fun register(req: RegisterRequest): Unit {
        if (userRepository.findByEmail(req.email).isPresent) {
            throw DuplicateEmailException("Email already exists")
        }

        val user = UserEntity(
            email = req.email,
            password = passwordEncoder.encode(req.password),
            nickname = req.nickname,
            roles = setOf(Role.ROLE_USER)
        )

        userRepository.save(user)
    }

    fun login(email: String, password: String): String {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UserNotFoundException("User not found") }

        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException("Invalid credentials")
        }
        val roles = user.roles.map { it.name }
        return jwtUtil.generateToken(user.email, roles)
    }
}
