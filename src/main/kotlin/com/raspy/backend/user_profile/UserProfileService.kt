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
class UserProfileService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val s3Uploader: S3Uploader,
) {
    companion object {
        private const val DEFAULT_PROFILE_PIC_URL =
            "https://d1iimlpplvq3em.cloudfront.net/service/default-profile.png"
    }

    @Transactional
    fun saveUserProfileInfo(
        age: Int,
        gender: Gender,
        region: Region,
        bio: String,
        profilePicture: MultipartFile?
    ) {
        // 현재 사용자 참조만 얻어오기 (외래키 설정용, 실제 SELECT는 지연)
        val userPrincipal = authService.getCurrentUser()
        val userId = userPrincipal.id
        val userRef = userRepository.getReferenceById(userId)

        log.info { "saveUserProfileInfo 시작: userId=$userId" }

        // 기존 프로필 조회 혹은 신규 생성
        val profile = userProfileRepository.findByUserId(userId)
            ?.apply {
                log.debug { "Existing profile found for userId=$userId, updating fields" }
            }
            ?: UserProfileEntity(user = userRef).also {
                log.debug { "No profile found for userId=$userId, creating new UserProfileEntity" }
            }

        // 공통 필드 설정
        profile.apply {
            this.age = age
            this.gender = gender
            this.region = region
            this.bio = bio
        }

        // 프로필 사진 업로드 혹은 기본 URL 설정
        val imageUrl = profilePicture
            ?.takeIf { it!=null && ! it.isEmpty }
            ?.also { log.debug { "Uploading new profile picture for userId=$userId" } }
            ?.let {
                try {
                    s3Uploader.upload(it)
                } catch (e: Exception) {
                    log.error(e) { "프로필 사진 업로드 실패, 기본 이미지로 대체 userId=$userId" }
                    DEFAULT_PROFILE_PIC_URL
                }
            }
            ?: run {
                log.debug { "No picture provided or empty—using default image for userId=$userId" }
                DEFAULT_PROFILE_PIC_URL
            }

        profile.profilePicture = imageUrl

        userProfileRepository.save(profile)
        log.info { "프로필 저장 완료: userId=$userId, profileId=${profile.id}" }
    }
}
