package com.raspy.backend.user_profile

import com.raspy.backend.auth.AuthService
import com.raspy.backend.s3.S3Uploader
import com.raspy.backend.user.UserRepository
import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private val log = KotlinLogging.logger {}

@Service
class UserProfileService (
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val s3Uploader: S3Uploader,
) {
    @Transactional
    fun saveUserProfileInfo(
        age: Int,
        gender: Gender,
        region: Region,
        bio: String,
        profilePicture: MultipartFile?
    ) {
        val userPrincipal = authService.getCurrentUser()
        val userId = userPrincipal.id

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        log.info { "Processing user profile update for userId: $userId" }

        val profile = userProfileRepository.findByUserId(userId)
            ?: UserProfileEntity(user = user)

        profile.age = age
        profile.gender = gender
        profile.region = region
        profile.bio = bio

        val imageUrl = if (profilePicture != null && !profilePicture.isEmpty) {
            log.debug { "Uploading profile picture for userId: $userId" }
            s3Uploader.upload(profilePicture)
        } else {
            "https://d1iimlpplvq3em.cloudfront.net/service/default-profile.png" // default profile image URL
        }

        profile.profilePicture = imageUrl

        userProfileRepository.save(profile)

        log.info { "Profile successfully saved/updated for userId: $userId" }
    }
}
