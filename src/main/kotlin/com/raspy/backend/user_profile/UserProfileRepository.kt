package com.raspy.backend.user_profile

import org.springframework.data.jpa.repository.JpaRepository

interface UserProfileRepository: JpaRepository<UserProfileEntity, Long> {
    fun findByUserId(userId: Long): UserProfileEntity?

}