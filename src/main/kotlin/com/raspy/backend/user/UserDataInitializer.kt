package com.raspy.backend.user

import com.raspy.backend.user.enumerated.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

@Configuration
class UserDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.email}") private val adminEmail: String,
    @Value("\${admin.password}") private val adminPassword: String
) {

    @Bean
    fun initData() = CommandLineRunner {
        val email = "test@test.test"

        /**
         * 테스트 유저 생성
         */
        if (!userRepository.existsByEmail(adminEmail)) {
            val user = UserEntity(
                email = email,
                password = passwordEncoder.encode("12341234"),
                nickname = "Test_User"
            )

            userRepository.save(user)
            log.info { "Test user created: $email / 1234" }
        }

        /**
         * 어드민 유저 생성
         */
        if (!userRepository.existsByEmail(adminEmail)) {
            val admin = UserEntity(
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword),
                nickname = "ADMIN",
                roles = setOf(Role.ROLE_ADMIN)
            )
            userRepository.save(admin)
            log.info { "Admin user created: $adminEmail / (hidden)" }
        }
    }
}
