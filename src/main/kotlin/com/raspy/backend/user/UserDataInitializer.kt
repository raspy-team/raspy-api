package com.raspy.backend.user

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class UserDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData() = CommandLineRunner {
        val email = "test"

        val user = UserEntity(
            email = email,
            password = passwordEncoder.encode("test"),
            nickname = "Test_User"
        )

        userRepository.save(user)
        println("[Test user created: $email / test]")
    }
}