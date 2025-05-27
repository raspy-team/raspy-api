package com.raspy.backend.game_play

import com.raspy.backend.auth.AuthService
import com.raspy.backend.game_play.response.GameDetailResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
class GamePlayController(
    private val gamePlayService: GamePlayService,
    private val authService: AuthService
) {
    @GetMapping("/{id}/detail")
    fun getDetail(@PathVariable id: Long): ResponseEntity<GameDetailResponse> {
        val user = authService.getCurrentUserEntity()
        return ResponseEntity.ok(gamePlayService.getDetail(id, user))
    }
}
