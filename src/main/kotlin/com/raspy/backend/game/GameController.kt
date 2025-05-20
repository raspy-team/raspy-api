package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.game.request.ApproveRequest
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.response.GameApplicantsResponse
import com.raspy.backend.game.response.GameSummaryResponse
import com.raspy.backend.game.response.MyGameResponse
import com.raspy.backend.game.response.RequestedGameResponse
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
    private val authService: AuthService
) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/create")
    @Operation(summary = "Create Custom Game", description = "사용자가 커스텀한 스포츠 경기를 생성합니다.")
    fun createGame(@Valid @RequestBody request: CreateGameRequest): ResponseEntity<String> {
        val userPrincipal = authService.getCurrentUser()
        log.info { "Game 생성 요청: user=${userPrincipal.id}" }

        gameService.createGame(request, userPrincipal.id)
        return ResponseEntity.ok("game is created")
    }

    @PutMapping("/{gameId}/update")
    @Operation(summary = "게임 정보 수정", description = "기존에 생성된 게임의 정보를 수정합니다.")
    fun updateGame(@PathVariable gameId: Long, @Valid @RequestBody request: CreateGameRequest): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "게임 수정 요청: game=$gameId by user=\${principal.id}" }

        gameService.updateGame(gameId, request, principal.id)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{gameId}/delete")
    @Operation(summary = "게임 삭제", description = "사용자가 본인이 생성한 게임을 삭제합니다.")
    fun deleteGame(@PathVariable gameId: Long): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "게임 삭제 요청: game=$gameId by user=\${principal.id}" }

        gameService.deleteGame(gameId, principal.id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/list")
    @Operation(
        summary = "게임 리스트 조회",
        description = "현재 참여 가능한 모든 게임 방의 요약 정보(제목, 카테고리, 간략 설명 등)를 반환합니다.",
        responses = [ApiResponse(responseCode = "200", description = "게임 리스트 반환 성공")]
    )
    fun listGames(): ResponseEntity<List<GameSummaryResponse>> {
        log.info { "전체 게임 리스트 요청" }
        return ResponseEntity.ok(gameService.findAllSummaries())
    }

    @GetMapping("/summary")
    @Operation(
        summary = "게임 간략 정보 조회",
        description = "게임 ID를 기반으로 간략한 게임 정보를 반환합니다.",
        responses = [ApiResponse(responseCode = "200", description = "게임 정보 반환 성공")]
    )
    fun gameInfo(@RequestParam("gameId") gameId: Long): ResponseEntity<GameSummaryResponse> {
        log.info { "게임 요약 요청: gameId=$gameId" }
        return ResponseEntity.ok(gameService.getGameSummary(gameId))
    }

    @PostMapping("/{gameId}/apply")
    @Operation(
        summary = "게임 참가 신청",
        description = "현재 로그인한 사용자가 해당 게임에 참가 신청을 보냅니다."
    )
    fun applyToJoin(@PathVariable gameId: Long): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "참가 신청: user=${principal.id}, game=$gameId" }

        gameService.applyToJoinGame(gameId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/approve/{participationId}")
    @Operation(
        summary = "게임 참가자 승인",
        description = "방장이 특정 참가자의 신청을 승인합니다."
    )
    fun approveParticipant(@PathVariable participationId: Long): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "참가자 승인 요청: host=${principal.id}, participation=$participationId" }

        gameService.approveParticipant(participationId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/cancel-approve")
    @Operation(summary = "참가 승인 취소", description = "참가자의 승인을 취소합니다.")
    fun cancelApprove(@RequestBody request: ApproveRequest) {
        val user = authService.getCurrentUserEntity()
        gameService.cancelApprove(request.gameId, request.userId, user)
    }


    @DeleteMapping("/{gameId}/leave")
    @Operation(
        summary = "게임 나가기",
        description = "현재 로그인한 사용자를 해당 게임에서 제외합니다."
    )
    fun leaveGame(@PathVariable gameId: Long): ResponseEntity<Unit> {
        val principal = authService.getCurrentUser()
        log.info { "게임 나가기 요청: user=${principal.id}, game=$gameId" }

        gameService.leaveGame(gameId, principal.id)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "내가 신청한 게임 목록 조회",
        description = "현재 로그인한 사용자가 참가 신청한 게임 목록(요약)을 반환합니다."
    )
    @GetMapping("/my-requests")
    fun getMyRequestedGames(): ResponseEntity<List<RequestedGameResponse>> {
        val user = authService.getCurrentUserEntity()
        log.info { "참가 신청 목록 요청: user=${user.id}" }

        val games = gameService.getRequestedGamesBy(user)
        return ResponseEntity.ok(games)
    }

    @PostMapping("/cancel-request")
    @Operation(summary = "게임 신청 취소", description = "지원자가 게임 신청을 취소합니다.")
    fun cancelGameRequest(@RequestBody req: CancelGameRequest) {
        val user = authService.getCurrentUserEntity()
        gameService.cancelMyRequest(req.gameId, user)
    }

    data class CancelGameRequest(val gameId: Long)

    @GetMapping("/my-games/applicants")
    @Operation(summary = "내가 생성한 게임에 지원한 유저 목록 조회", description = "로그인한 사용자가 만든 게임에 신청한 유저 정보를 그룹별로 조회합니다.")
    fun getApplicantsForMyGames(): List<GameApplicantsResponse> {
        val user = authService.getCurrentUserEntity()
        return gameService.getApplicantsForGamesCreatedBy(user)
    }

    @PostMapping("/approve")
    @Operation(summary = "참가 신청 승인", description = "특정 게임에 지원한 사용자의 참가 신청을 승인합니다.")
    fun approveApplicant(@RequestBody request: ApproveRequest) {
        val user = authService.getCurrentUserEntity()
        gameService.approveApplicant(request.gameId, request.userId, user)
    }

    @Operation(
        summary = "참가 신청 ID 조회",
        description = "gameId와 email로 참가 신청 엔티티의 ID를 조회합니다."
    )
    @GetMapping("/{gameId}/participants/{email}")
    fun getParticipationIdByGameAndEmail(
        @PathVariable gameId: Long,
        @PathVariable email: String
    ): ResponseEntity<Long> {
        val id = gameService.findParticipationId(gameId, email)
        return ResponseEntity.ok(id)
    }


//    @Operation(
//        summary = "진행 예정인 게임 목록 조회",
//        description = "승인된 상태이고, 아직 시작 전인 내 게임 목록을 반환합니다."
//    )
//    @GetMapping("/my-approved-games")
//    fun getMyApprovedGames(): ResponseEntity<List<GameSummaryResponse>> {
//        val principal = authService.getCurrentUser()
//        log.info { "진행 예정 게임 요청: user=${principal.id}" }
//
//        val games = gameService.getMyApprovedGames()
//        return ResponseEntity.ok(games)
//    }


    @GetMapping("/my-games")
    @Operation(summary = "내 게임 전체 조회", description = "내가 참가자로 속한 모든 게임 정보를 반환합니다. (진행 예정, 완료 포함)")
    fun getMyGames(): List<MyGameResponse> {
        val user = authService.getCurrentUserEntity()
        return gameService.getMyIncomingGames(user)
    }


    @PostMapping("/{id}/start")
    @Operation(summary = "Start Game", description = "경기 주최자가 해당 게임을 시작 상태로 변경합니다.")
    fun startGame(@PathVariable id: Long): ResponseEntity<Unit> {
        val user = authService.getCurrentUser()
        gameService.startGame(id, user)
        return ResponseEntity.ok().build()
    }


}
