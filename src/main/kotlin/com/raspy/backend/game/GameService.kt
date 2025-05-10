package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.game.enumerated.WinCondition
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.response.GameSummaryResponse
import com.raspy.backend.user.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
) {
    private val log = KotlinLogging.logger {}

    fun createGame(request: CreateGameRequest, userId: Long): Unit {
        log.info { "createGame: userId=$userId, major=${request.majorCategory}, minor=${request.minorCategory}" }

        // 외래키 설정용으로만 참조, 실제 SELECT는 지연(fetch)
        val userRef = userRepository.getReferenceById(userId)

        val game = GameEntity(
            title             = request.title,
            description       = request.description,
            rulesDescription  = request.rulesDescription,
            majorCategory     = request.majorCategory,
            minorCategory     = request.minorCategory,
            pointsToWin       = request.pointsToWin,
            setsToWin         = request.setsToWin,
            duration          = request.duration,
            winBy             = WinCondition.valueOf(request.winBy),
            matchDate         = request.matchDate,
            placeRoad         = request.placeRoad,
            placeDetail       = request.placeDetail,
            participants      = setOf(userRepository.findById(authService.getCurrentUser().id).get()),
            createdBy         = userRef,
            createdAt         = LocalDateTime.now()
        )
        log.debug { "Saving GameEntity: $game" }
        val saved = gameRepository.save(game)
        log.info { "Game created (id=${saved.id})" }
    }

    fun findAllSummaries(): List<GameSummaryResponse> {
        val games = gameRepository.findAll()  // 단순한 경우 fetch join 불필요

        return games.map { game ->
            GameSummaryResponse(
                id = game.id,
                title = game.title,
                majorCategory = game.majorCategory,
                minorCategory = game.minorCategory,
                description = game.description,
                currentParticipantCounts = game.participants.size,
                maxPlayers = game.maxPlayers,
                matchDate = game.matchDate,
                matchLocation = formatMatchLocation(game.placeRoad, game.placeDetail)
            )
        }
    }

    private fun formatMatchLocation(road: String?, detail: String?): String? {
        return when {
            road != null && detail != null -> "$road $detail"
            road != null -> road
            detail != null -> detail
            else -> null
        }
    }

}