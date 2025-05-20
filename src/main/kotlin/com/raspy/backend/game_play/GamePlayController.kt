package com.raspy.backend.game_play

import com.raspy.backend.auth.AuthService
import com.raspy.backend.game.GameService
import com.raspy.backend.game_play.request.ReviewRequest
import com.raspy.backend.game_play.request.ScoreLogRequest
import com.raspy.backend.game_play.response.GameDetailResponse
import com.raspy.backend.game_play.response.GameResultResponse
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

    /**
     *
     *
     *
     * TODO: WS로 구현
     *
     *
     *
     */
    @PostMapping("/{id}/score-log")
    fun changeScore(@PathVariable id: Long, @RequestBody req: ScoreLogRequest): ResponseEntity<Unit> {
        val user = authService.getCurrentUserEntity()
        gamePlayService.logScore(id, user, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/next-set")
    fun nextSet(@PathVariable id: Long): ResponseEntity<Unit> {
        gamePlayService.moveToNextSet(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/pause")
    fun pause(@PathVariable id: Long): ResponseEntity<Unit> {
        gamePlayService.pauseGame(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/finish")
    fun finish(@PathVariable id: Long): ResponseEntity<Unit> {
        gamePlayService.finishGame(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/result")
    fun getResult(@PathVariable id: Long): ResponseEntity<GameResultResponse> {
        return ResponseEntity.ok(gamePlayService.getResult(id))
    }

    @PostMapping("/{id}/review")
    fun submitReview(@PathVariable id: Long, @RequestBody review: ReviewRequest): ResponseEntity<Unit> {
        gamePlayService.submitReview(id, review)
        return ResponseEntity.ok().build()
    }
}
