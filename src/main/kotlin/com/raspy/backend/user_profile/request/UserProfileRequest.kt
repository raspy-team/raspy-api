package com.raspy.backend.user_profile.request

import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UserProfileRequest(
    @field:NotNull
    @field:Min(0)  // 나이는 0 이상
    @field:Max(150)  // 현실적으로 최대 150살로 제한
    val age: Int,

    @field:NotNull
    val gender: Gender,

    @field:NotNull
    val region: Region,

    @field:NotBlank
    @field:Size(max = 500)  // bio는 최대 500자 제한
    val bio: String
)
