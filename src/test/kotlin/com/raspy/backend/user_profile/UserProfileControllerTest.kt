package com.raspy.backend.user_profile

import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userProfileService: UserProfileService

    @Test
    @WithMockUser
    fun `프로필 저장 성공 - 정상 요청`() {
        val file = MockMultipartFile("profile_picture", "test.jpg", "image/jpeg", byteArrayOf(1, 2))

        mockMvc.perform(
            multipart("/user-profile/save")
                .file(file)
                .param("age", "25")
                .param("gender", "M")
                .param("region", "SEOUL")
                .param("bio", "안녕하세요")
                .with { it.method = "PATCH"; it }
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Profile setup completed"))

        verify(userProfileService).saveUserProfileInfo(
            age = 25,
            gender = Gender.M,
            region = Region.SEOUL,
            bio = "안녕하세요",
            profilePicture = file
        )
    }

    @Test
    @WithMockUser
    fun `프로필 저장 성공 - 바이오 생략`() {
        val file = MockMultipartFile("profile_picture", "test.jpg", "image/jpeg", byteArrayOf(1))

        mockMvc.perform(
            multipart("/user-profile/save")
                .file(file)
                .param("age", "30")
                .param("gender", "F")
                .param("region", "BUSAN")
                .param("bio", "")
                .with { it.method = "PATCH"; it }
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun `프로필 저장 성공 - PNG 이미지`() {
        val file = MockMultipartFile("profile_picture", "profile.png", "image/png", byteArrayOf(1, 2))

        mockMvc.perform(
            multipart("/user-profile/save")
                .file(file)
                .param("age", "20")
                .param("gender", "O")
                .param("region", "SEOUL")
                .param("bio", "테스트 사용자")
                .with { it.method = "PATCH"; it }
        ).andExpect(status().isOk)
    }

    @Test
    fun `프로필 저장 실패 - 인증 없음`() {
        val file = MockMultipartFile("profile_picture", "test.jpg", "image/jpeg", byteArrayOf(1))

        mockMvc.perform(
            multipart("/user-profile/save")
                .file(file)
                .param("age", "25")
                .param("gender", "F")
                .param("region", "SEOUL")
                .param("bio", "내용")
                .with { it.method = "PATCH"; it }
        ).andExpect(status().isForbidden) // not implement 401(unauthorized error)
    }

    @Test
    @WithMockUser
    fun `프로필 저장 실패 - 나이 없음`() {
        val file = MockMultipartFile("profile_picture", "test.jpg", "image/jpeg", byteArrayOf(1))

        mockMvc.perform(
            multipart("/user-profile/save")
                .file(file)
                .param("gender", "O")
                .param("region", "SEOUL")
                .param("bio", "내용")
                .with { it.method = "PATCH"; it }
        ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `프로필 저장 성공 - 프로필 사진 없음`() {
        mockMvc.perform(
            multipart("/user-profile/save")
                .param("age", "22")
                .param("gender", "M")
                .param("region", "SEOUL")
                .param("bio", "내용")
                .with { it.method = "PATCH"; it }
        ).andExpect(status().isOk)

        verify(userProfileService).saveUserProfileInfo(
            age = 22,
            gender = Gender.M,
            region = Region.SEOUL,
            bio = "내용",
            profilePicture = null
        )
    }
}
