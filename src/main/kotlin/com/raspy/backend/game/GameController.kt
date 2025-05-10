package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.response.GameSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService,
    private val authService: AuthService       // ← 추가
) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/create")
    @Operation(summary = "Create Custom Game", description = "사용자가 커스텀한 스포츠 경기를 생성합니다.")
    fun createGame(@Valid @RequestBody request: CreateGameRequest): ResponseEntity<String> {
        // UserPrincipal을 AuthService에서 꺼내오면
        val userPrincipal = authService.getCurrentUser()
        log.info { "User(${userPrincipal.id}) 요청으로 Game 생성 시작" }

        gameService.createGame(request, userPrincipal.id)

        log.info { "User(${userPrincipal.id}) 요청 Game 생성 완료: ${request.title}" }
        return ResponseEntity.ok("game is created")
    }

    @Operation(
        summary = "게임 리스트 조회",
        description = "현재 참여 가능한 모든 게임 방의 요약 정보(제목, 카테고리, 간략 설명 등)를 반환합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "게임 리스트 반환 성공")
        ]
    )
    @GetMapping
    fun listGames(): ResponseEntity<List<GameSummaryResponse>> {
        log.info { "전체 게임 리스트 요청" }
        val gameSummaryResponses = gameService.findAllSummaries()
        return ResponseEntity.ok(gameSummaryResponses)
    }

    @Operation(
        summary     = "게임 참가",
        description = "현재 로그인한 사용자를 해당 게임에 참가시킵니다."
    )
    @PostMapping("/{gameId}/join")
    fun joinGame(
        @PathVariable gameId: Long,
    ): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "JOIN 요청: user=${principal.id}, game=$gameId" }
        gameService.joinGame(gameId, principal.id)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary     = "게임 나가기",
        description = "현재 로그인한 사용자를 해당 게임에서 제외합니다."
    )
    @DeleteMapping("/{gameId}/leave")
    fun leaveGame(
        @PathVariable gameId: Long,
    ): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "LEAVE 요청: user=${principal.id}, game=$gameId" }
        gameService.leaveGame(gameId, principal.id)
        return ResponseEntity.ok().build()
    }

}
