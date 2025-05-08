    package com.raspy.backend.game.request

    import jakarta.validation.constraints.NotNull
    import jakarta.validation.constraints.Min
    import jakarta.validation.constraints.Size
    import java.time.LocalDateTime

    data class CreateGameRequest(
        @field:NotNull
        @field:Size(min = 1, max = 50)
        val title: String, // 게임 제목

        @field:NotNull
        @field:Size(min = 1, max = 255)
        val description: String, // 게임 내용

        @field:NotNull
        @field:Size(min = 1, max = 500)
        val rulesDescription: String, // 규칙 설명

        @field:NotNull
        @field:Size(min = 1, max = 50)
        val majorCategory: String, // 대분류

        @field:NotNull
        @field:Size(min = 1, max = 50)
        val minorCategory: String, // 소분류

        @field:Min(value = -1)
        val pointsToWin: Int, // points_to_win (무제한일 경우 -1)

        @field:Min(value = 1)
        val setsToWin: Int, // sets_to_win (최소 1)

        @field:Min(value = -1)
        val duration: Int, // duration (초 단위, 무제한은 -1)

        @field:NotNull
        val winBy: String, // "SETS_HALF_WIN" or "MOST_SETS_AND_POINTS"

        val matchDate: LocalDateTime?, // 경기 일시 (협의 가능일 경우 null)

        val placeRoad: String?, // 도로명 주소 (협의 가능일 경우 null)

        val placeDetail: String?, // 상세주소 (협의 가능일 경우 null)

//        @field:Min(value = 2)
//        val participants: Int // 기본값 2명
    )
