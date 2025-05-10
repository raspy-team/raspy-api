package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.chat.ChatRoomEntity
import com.raspy.backend.chat.ChatRoomRepository
import com.raspy.backend.chat.ChatRoomType
import com.raspy.backend.game.enumerated.WinCondition
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.response.GameSummaryResponse
import com.raspy.backend.user.UserRepository
import com.raspy.backend.user.UserService
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val chatRoomRepository: ChatRoomRepository,
    private val participationRepository: ParticipationRepository,
    private val userService: UserService,

    ) {
    private val log = KotlinLogging.logger {}

    fun createGame(request: CreateGameRequest, userId: Long): Unit {
        log.info { "createGame: userId=$userId, major=${request.majorCategory}, minor=${request.minorCategory}" }

        // 외래키 설정용으로만 참조, 실제 SELECT는 지연(fetch)
        val userRef = userRepository.getReferenceById(userId)

        val game = GameEntity(
            title             = request.title,
            description       = request.description,
            rule = RuleEntity(
                rulesDescription  = request.rulesDescription,
                majorCategory     = request.majorCategory,
                minorCategory     = request.minorCategory,
                pointsToWin       = request.pointsToWin,
                setsToWin         = request.setsToWin,
                duration          = request.duration,
                winBy             = WinCondition.valueOf(request.winBy),
            ),
            matchDate         = request.matchDate,
            placeRoad         = request.placeRoad,
            placeDetail       = request.placeDetail,
            createdBy         = userRef,
            createdAt         = LocalDateTime.now()
        )

        val participation = ParticipationEntity(
            game     = game,
            user     = userRef,
        )
        game.participations.add(participation)

        log.debug { "Saving GameEntity: $game" }
        val saved = gameRepository.save(game)
        log.info { "Game created (id=${saved.id})" }
    }

    fun findAllSummaries(): List<GameSummaryResponse> {
        val games = gameRepository.findAllAtGameList()  // 단순한 경우 fetch join 불필요

        return games.map { game ->
            GameSummaryResponse(
                id = game.id,
                title = game.title,
                majorCategory = game.rule.majorCategory,
                minorCategory = game.rule.minorCategory,
                description = game.description,
                currentParticipantCounts = game.participations.size,
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

    @Transactional
    fun joinGame(gameId: Long, userId: Long) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임을 찾을 수 없습니다: $gameId") }
        val user = userRepository.getReferenceById(userId)

        if (game.participations.any { it.id == userId }) {
            throw IllegalStateException("이미 참가한 유저입니다.")
        }

        game.participations.add(
            ParticipationEntity(
                game     = game,
                user = user,
            )
        )
        log.debug { "참가자 추가 완료: user=$userId to game=$gameId" }

        participationRepository.save(
            ParticipationEntity(
                game = game,
                user = user,
                joinedAt = LocalDateTime.now()
            )
        )

        chatRoomRepository.findByGame(game) ?: run {
            chatRoomRepository.save(
                ChatRoomEntity(
                    type = ChatRoomType.GAME,
                    game = game
                )
            )
            log.debug { "ChatRoomEntity 생성: game=$gameId" }
        }

        gameRepository.save(game)
        log.info { "게임 참가 처리 완료: user=$userId -> game=$gameId" }
    }

    @Transactional
    fun leaveGame(gameId: Long, userId: Long) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임을 찾을 수 없습니다: $gameId") }
        val user = userRepository.getReferenceById(userId)

        if (game.participations.none { it.id == userId }) {
            throw IllegalStateException("참가 중인 유저가 아닙니다.")
        }

        game.participations.removeIf { it.id == userId }
        log.debug { "참가자 제거 완료: user=$userId from game=$gameId" }

         participationRepository.findByGameAndUser(game, user)?.let {
             it.leftAt = LocalDateTime.now()
         }

        gameRepository.save(game)
        log.info { "게임 나가기 처리 완료: user=$userId from game=$gameId" }
    }

}