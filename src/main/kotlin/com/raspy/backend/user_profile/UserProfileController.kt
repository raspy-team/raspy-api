package com.raspy.backend.user_profile

import com.raspy.backend.jwt.UserPrincipal
import com.raspy.backend.user_profile.request.UserProfileRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/user-profile")
class UserProfileController(
    private val userProfileService: UserProfileService
) {

    /**
     * 프로필 생성, 수정
     */
    @Operation(
        summary = "사용자 프로필 저장",
        description = "나이, 성별, 지역, 자기소개, 프로필 사진을 저장하거나 수정합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    @PatchMapping("/save")
    fun saveUserProfile(
        @ModelAttribute request: UserProfileRequest,  // multipart/form-data의 이미지가 아닌 일반 필드들을 DTO로 매핑
        @RequestParam("profile_picture") profilePicture: MultipartFile,
    ): ResponseEntity<String> {
        userProfileService.saveUserProfileInfo(
            age = request.age,
            gender = request.gender,
            region = request.region,
            bio = request.bio,
            profilePicture = profilePicture
        )
        return ResponseEntity.ok("Profile setup completed")
    }

}