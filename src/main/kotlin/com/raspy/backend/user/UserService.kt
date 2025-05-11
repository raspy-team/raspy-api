package com.raspy.backend.user

import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import com.raspy.backend.user_profile.UserProfileEntity
import org.springframework.stereotype.Service

@Service
class UserService(
     private val userRepository: UserRepository
) {
    fun getNickname(email: String): String = userRepository.findNicknameByEmail(email)?.nickname?: throw RuntimeException("없는 이메일이야~~")
    fun getUserEntity(email: String): UserEntity = userRepository.findByEmail(email).get()
}