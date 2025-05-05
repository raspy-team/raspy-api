package com.raspy.backend.Auth

import com.raspy.backend.Auth.Request.RegisterRequest
import com.raspy.backend.Exception.DuplicateEmailException
import com.raspy.backend.Exception.InvalidCredentialsException
import com.raspy.backend.Exception.UserNotFoundException
import com.raspy.backend.Jwt.JwtUtil
import com.raspy.backend.User.Role
import com.raspy.backend.User.UserEntity
import com.raspy.backend.User.UserRepository
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
