package com.raspy.backend.user_profile.request

import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region

data class UserProfileRequest(
    val age: Int,
    val gender: Gender,
    val region: Region,
    val bio: String
)