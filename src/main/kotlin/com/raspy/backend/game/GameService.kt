package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.chat.ChatRoomEntity
import com.raspy.backend.chat.ChatRoomRepository
import com.raspy.backend.chat.ChatRoomType
import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.game.enumerated.WinCondition
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.request.UpdateGameRequest
import com.raspy.backend.game.response.GameSummaryResponse
import com.raspy.backend.user.UserEntity
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

    fun createGame(request: CreateGameRequest, userId: Long) {
        val user = getUserRef(userId)

        val game = GameEntity(
            title = request.title,
            description = request.description,
            rule = RuleEntity(
                rulesDescription = request.rulesDescription,
                majorCategory = request.majorCategory,
                minorCategory = request.minorCategory,
                pointsToWin = request.pointsToWin,
                setsToWin = request.setsToWin,
                duration = request.duration,
                winBy = WinCondition.valueOf(request.winBy),
            ),
            matchDate = request.matchDate,
            placeRoad = request.placeRoad,
            placeDetail = request.placeDetail,
            createdBy = user,
            createdAt = LocalDateTime.now()
        )

        game.participations.add(
            ParticipationEntity(game = game, user = user, status = ParticipationStatus.APPROVED)
        )

        gameRepository.save(game).also {
            log.info { "게임 생성 완료: id=${it.id}, by=${user.id}" }
        }
    }

    @Transactional
    fun updateGame(gameId: Long, request: UpdateGameRequest, userId: Long) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임을 찾을 수 없습니다: $gameId") }

        if (game.createdBy.id != userId) {
            throw IllegalAccessException("본인이 생성한 게임만 수정할 수 있습니다.")
        }

        game.apply {
            title = request.title
            description = request.description
            matchDate = request.matchDate
            placeRoad = request.placeRoad
            placeDetail = request.placeDetail
            maxPlayers = request.maxPlayers
            modifiedAt = LocalDateTime.now()
        }

        log.info { "게임 수정 완료: $gameId by user=$userId" }
    }

    @Transactional
    fun deleteGame(gameId: Long, userId: Long) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임을 찾을 수 없습니다: $gameId") }

        if (game.createdBy.id != userId) {
            throw IllegalAccessException("본인이 생성한 게임만 삭제할 수 있습니다.")
        }

        gameRepository.delete(game)
        log.info { "게임 삭제 완료: $gameId by user=$userId" }
    }


    fun getGameSummary(id: Long): GameSummaryResponse =
        getGameOrThrow(id).toSummary()

    fun findAllSummaries(): List<GameSummaryResponse> =
        gameRepository.findAllAtGameList().map { it.toSummary() }

    @Transactional
    fun applyToJoinGame(gameId: Long) {
        val user = authService.getCurrentUserEntity()
        val game = getGameOrThrow(gameId)

        if (game.createdBy.id == user.id)
            throw Exception("자신이 생성한 게임에는 신청할 수 없습니다.")

        if (participationRepository.existsByGameAndUser(game, user))
            throw Exception("이미 신청한 게임입니다.")


        participationRepository.save(
            ParticipationEntity(
                game = game,
                user = user,
                status = ParticipationStatus.REQUESTED
            )
        )

        log.info { "참가 신청: user=${user.id}, game=$gameId" }
    }

    @Transactional
    fun approveParticipant(participationId: Long) {
        val host = authService.getCurrentUser()
        val participation = participationRepository.findById(participationId)
            .orElseThrow { NoSuchElementException("참가 정보 없음: $participationId") }

        if (participation.game.createdBy.id != host.id) {
            throw IllegalAccessException("해당 게임의 방장만 승인 가능")
        }

        participation.status = ParticipationStatus.APPROVED
        participationRepository.save(participation)

        chatRoomRepository.findByGame(participation.game) ?: chatRoomRepository.save(
            ChatRoomEntity(type = ChatRoomType.GAME, game = participation.game)
        )

        log.info { "참가 승인: user=${participation.user.id}, game=${participation.game.id}" }
    }

    @Transactional
    fun leaveGame(gameId: Long, userId: Long) {
        val game = getGameOrThrow(gameId)
        val user = getUserRef(userId)

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw IllegalStateException("참여 이력 없음")

        participation.leftAt = LocalDateTime.now()
        game.participations.removeIf { it.id == participation.id }

        log.info { "게임 나가기: user=${user.id}, game=$gameId" }
    }

    fun getMyRequestedGames(): List<GameSummaryResponse> {
        val user = authService.getCurrentUserEntity()
        return participationRepository.findAllByUserAndStatus(user, ParticipationStatus.REQUESTED)
            .map { it.game.toSummary() }
    }

    fun getApplicantsForMyGames(): List<Pair<Long, String>> {
        val user = authService.getCurrentUserEntity()
        val myGames = gameRepository.findAllByCreatedBy(user)

        return participationRepository.findAllByGameInAndStatus(myGames, ParticipationStatus.REQUESTED)
            .map { it.game.id to it.user.email }
    }

    fun findParticipationId(gameId: Long, email: String): Long {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임 없음: $gameId") }
        val user = userRepository.findByEmail(email).orElseThrow{throw NoSuchElementException("유저 없음: $email")}

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw NoSuchElementException("참여 신청 내역 없음")

        return participation.id
    }

    fun getMyApprovedGames(): List<GameSummaryResponse> {
        val user: UserEntity = authService.getCurrentUserEntity()
        val now = LocalDateTime.now()

        return participationRepository
            .findAllByUserAndStatus(user, ParticipationStatus.APPROVED)
            .map { it.game }
            .filter { game ->
                game.matchDate?.isAfter(now) ?: true
            }
            .map { it.toSummary() }
    }

    private fun getGameOrThrow(id: Long): GameEntity =
        gameRepository.findById(id).orElseThrow { NoSuchElementException("게임 없음: $id") }

    private fun getUserRef(id: Long) = userRepository.getReferenceById(id)

    private fun GameEntity.toSummary(): GameSummaryResponse =
        GameSummaryResponse(
            id = this.id,
            title = this.title,
            majorCategory = this.rule.majorCategory,
            minorCategory = this.rule.minorCategory,
            description = this.description,
            currentParticipantCounts = this.participations.count { it.status == ParticipationStatus.APPROVED },
            maxPlayers = this.maxPlayers,
            matchDate = this.matchDate,
            matchLocation = formatMatchLocation(this.placeRoad, this.placeDetail)
        )

    private fun formatMatchLocation(road: String?, detail: String?): String? =
        listOfNotNull(road, detail).take(2).joinToString(" ")
}
