package com.raspy.backend.user_profile

import com.raspy.backend.auth.AuthService
import com.raspy.backend.s3.S3Uploader
import com.raspy.backend.user.UserRepository
import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserProfileService (
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val s3Uploader: S3Uploader,
){
    @Transactional
    fun saveUserProfileInfo(
        age: Int,
        gender: Gender,
        region: Region,
        bio: String,
        profilePicture: MultipartFile
    ) {
        val userPrincipal = authService.getCurrentUser()
        val userId = userPrincipal.id

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val profile = userProfileRepository.findByUserId(userId)
            ?: UserProfileEntity(user = user)

        profile.age = age
        profile.gender = gender
        profile.region = region
        profile.bio = bio

        /**
         * s3 저장 필요 (현재는 테스트 이미지 url임) profilePicture
         */
        val imageUrl = s3Uploader.upload(profilePicture)
        profile.profilePicture = imageUrl

        userProfileRepository.save(profile)
    }

}