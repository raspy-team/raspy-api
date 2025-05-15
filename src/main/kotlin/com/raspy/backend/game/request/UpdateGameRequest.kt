package com.raspy.backend.game.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateGameRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val title: String,

    @field:NotBlank
    @field:Size(max = 255)
    val description: String,

    val matchDate: LocalDateTime?,
    val placeRoad: String?,
    val placeDetail: String?,
    val maxPlayers: Int
)