package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.chat.ChatRoomEntity
import com.raspy.backend.chat.ChatRoomRepository
import com.raspy.backend.chat.ChatRoomType
import com.raspy.backend.game.enumerated.GameStatus
import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.game.enumerated.WinCondition
import com.raspy.backend.game.request.CreateGameRequest
import com.raspy.backend.game.response.*
import com.raspy.backend.rule.RuleDto
import com.raspy.backend.rule.RuleEntity
import com.raspy.backend.rule.RuleRepository
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
    private val ruleRepository: RuleRepository,

    ) {
    private val log = KotlinLogging.logger {}

    fun createGame(request: CreateGameRequest, userId: Long) {
        val user = getUserRef(userId)

        val game = GameEntity(
            rule = request.referencedRuleId
                ?.let {
                    ruleRepository.findById(it).orElseThrow{RuntimeException("not exist rule id")}
                }
                ?: createRule(
                    RuleDto(
                        ruleDescription = request.ruleDescription!!,
                        pointsToWin = request.pointsToWin!!,
                        setsToWin = request.setsToWin!!,
                        duration = request.duration!!,
                        winBy = request.winBy!!
                    )
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

    fun createRule(ruleDto: RuleDto): RuleEntity {
        val title = createRuleTitleWithAI(ruleDto.ruleDescription)
        val categories = createRuleCategoryWithAI(ruleDto.ruleDescription)

        val rule = RuleEntity(
            ruleTitle = title,
            ruleDescription = ruleDto.ruleDescription,
            majorCategory = categories[0],
            minorCategory = categories[1],
            pointsToWin = ruleDto.pointsToWin,
            setsToWin = ruleDto.setsToWin,
            duration = ruleDto.duration,
            winBy = WinCondition.valueOf(ruleDto.winBy),
            createdBy = authService.getCurrentUser().email
        )

        return ruleRepository.save(rule)
    }

    private fun createRuleTitleWithAI(description: String): String = "에이아이가 할거다"

    private fun createRuleCategoryWithAI(description: String): Array<String> = arrayOf("메이저", "마이너")

    @Transactional
    fun startGame(id: Long, user: UserEntity) {
        val game = gameRepository.findById(id).orElseThrow { throw NoSuchElementException("게임이 존재하지 않음") }
        if (game.createdBy.id != user.id) throw IllegalAccessException("주최자만 경기를 시작할 수 있습니다.")
        game.gameStatus = GameStatus.IN_PROGRESS
        log.info { "Game 시작됨: id=$id" }
    }

    @Transactional
    fun updateGame(gameId: Long, request: CreateGameRequest, userId: Long) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임을 찾을 수 없습니다: $gameId") }

        if (game.createdBy.id != userId) {
            throw IllegalAccessException("본인이 생성한 게임만 수정할 수 있습니다.")
        }

        game.apply {
            matchDate = request.matchDate
            placeRoad = request.placeRoad
            placeDetail = request.placeDetail
            createdAt = LocalDateTime.now()
        }


        if(game.rule.ruleDescription != request.ruleDescription || game.rule.id != request.referencedRuleId)
            /**
             * title, description, category 재설정 필요
             */
            game.rule.apply {
                val categories =  createRuleCategoryWithAI(request.ruleDescription!!)

                ruleTitle = createRuleTitleWithAI(request.ruleDescription)
                ruleDescription = request.ruleDescription
                majorCategory = categories[0]
                minorCategory = categories[1]
            }


        game.rule.apply {
            pointsToWin = request.pointsToWin?:-1
            setsToWin = request.setsToWin?:-1
            duration = request.duration?:-1
            winBy = WinCondition.valueOf((request.winBy?:WinCondition.MOST_SETS_AND_POINTS).toString())
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

        gameRepository.updateGameStatusById(gameId, GameStatus.DELETED)
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

        if(game.gameStatus != GameStatus.MATCHING)
            throw Exception("지원이 마감된 게임입니다.")

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

        if (participation.game.createdBy.id != host.id)
            throw IllegalAccessException("해당 게임의 방장만 승인 가능")

        if(participation.game.gameStatus != GameStatus.MATCHING)
            throw IllegalAccessException("지원이 마감된 게임")

        val approvedCount = participationRepository.countByGameAndStatus(participation.game, ParticipationStatus.APPROVED)
        if (approvedCount >= 1) {
            // 생성자 1명 + 신청자 1명 → 2인 제한 초과
            throw IllegalStateException("최대 인원 초과")
        }

        participation.status = ParticipationStatus.APPROVED
        // TODO: 상태변하는지 테스트
        participation.game.gameStatus = GameStatus.SCHEDULED

        participation.game.participations.add(participation)
        participationRepository.save(participation)

        chatRoomRepository.findByGame(participation.game) ?: chatRoomRepository.save(
            ChatRoomEntity(type = ChatRoomType.GAME, game = participation.game)
        )

        log.info { "참가 승인: user=${participation.user.id}, game=${participation.game.id}" }
    }

    @Transactional
    fun cancelApprove(gameId: Long, userId: Long, requester: UserEntity) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임 없음: $gameId") }

        if (game.createdBy.id != requester.id) {
            throw IllegalAccessException("해당 게임의 생성자만 참가승인 취소 가능")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("유저 없음: $userId") }

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw NoSuchElementException("참여 정보 없음")

        if (participation.status != ParticipationStatus.APPROVED)
            throw IllegalStateException("이미 승인되지 않은 상태입니다.")

        participation.status = ParticipationStatus.REQUESTED
        participation.game.gameStatus = GameStatus.MATCHING

        participationRepository.save(participation)

        log.info { "참가 승인 취소: user=$userId, game=$gameId" }
    }

    @Transactional
    fun leaveGame(gameId: Long, userId: Long) {
        val game = getGameOrThrow(gameId)
        val user = getUserRef(userId)

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw IllegalStateException("참여 이력 없음")

        /**
         * 게임에 소속 되어있고, 진행 예정일 경우에만 이탈 가능
         */
        if(game.gameStatus != GameStatus.SCHEDULED) {
            throw IllegalAccessException("이탈 불가")
        }

        participation.leftAt = LocalDateTime.now()
        game.participations.removeIf { it.id == participation.id }
        game.gameStatus = GameStatus.MATCHING

        log.info { "게임 나가기: user=${user.id}, game=$gameId" }
    }

    fun getRequestedGamesBy(user: UserEntity): List<RequestedGameResponse> {
        val participations: List<ParticipationEntity> = participationRepository.findAllByUser(user)

        return participations
            .filter {
                it.status in listOf(ParticipationStatus.REQUESTED, ParticipationStatus.APPROVED)
                        && it.game.createdBy.id != user.id // 게임 생성자가 나면 안됨
            }
            .map { participation ->
                val game = participation.game
                val host = game.createdBy

                RequestedGameResponse(
                    id = game.id,
                    title = game.rule.ruleTitle,
                    description = game.rule.ruleDescription,
                    majorCategory = game.rule.majorCategory,
                    minorCategory = game.rule.minorCategory,
                    matchDate = game.matchDate,
                    matchLocation = game.placeRoad,
                    status = participation.status,
                    ownerNickname = host.nickname,
                    ownerProfileUrl = host.profile?.profilePicture,
                    gameStatus =  game.gameStatus,

                    //TODO: 통계 적용 시 구현
                    ownerWins = 20,
                    ownerLosses = 4,
                    ownerDraws = 1,
                    ownerRating = 75.0
                )
            }
    }

    @Transactional
    fun cancelMyRequest(gameId: Long, user: UserEntity) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임 없음") }

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw NoSuchElementException("신청 기록 없음")

        if (participation.status != ParticipationStatus.REQUESTED)
            throw IllegalStateException("이미 승인되었거나 취소된 신청은 취소할 수 없습니다.")

        participationRepository.delete(participation)
        log.info { "신청 취소: user=${user.id}, game=${game.id}" }
    }
    //TODO: N+1 해결해야함
    fun getApplicantsForGamesCreatedBy(owner: UserEntity): List<GameApplicantsResponse> {
        val games = gameRepository.findAllByCreatedBy(owner)

        return games.mapNotNull { game ->
            val applicants = participationRepository.findAllByGameOrderByIdDesc(game)
                .filter{
                    it.user.id != owner.id
                }
            if (applicants.isEmpty()) return@mapNotNull null

            GameApplicantsResponse(
                gameId = game.id,
                title = game.rule.ruleTitle,
                description = game.rule.ruleDescription,
                majorCategory = game.rule.majorCategory,
                minorCategory = game.rule.minorCategory,
                matchDate = game.matchDate,
                matchLocation = game.placeRoad,
                applicants = applicants.map {
                    val user = it.user
                    ApplicantInfo(
                        userId = user.id!!,
                        applicantNickname = user.nickname,
                        // TODO: 통계 구현 시 작업예정
                        wins = 20, // user.win,
                        losses = 3, // user.losses,
                        draws = 2, // user.draws,
                        rating = 80.0 , //user.rating,
                        approved = it.status == ParticipationStatus.APPROVED
                    )
                },
                gameStatus = game.gameStatus
            )
        }
    }

    @Transactional
    fun approveApplicant(gameId: Long, applicantId: Long, approver: UserEntity) {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("존재하지 않는 게임입니다.") }

        if (game.createdBy.id != approver.id)
            throw IllegalAccessException("해당 게임의 생성자만 승인할 수 있습니다.")

        val user = userRepository.findById(applicantId)
            .orElseThrow { NoSuchElementException("존재하지 않는 사용자입니다.") }

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw NoSuchElementException("해당 유저의 신청 내역이 없습니다.")

        if(game.gameStatus != GameStatus.MATCHING)
            throw IllegalAccessException("지원이 마감된 게임입니다.")

        participation.status = ParticipationStatus.APPROVED
        game.gameStatus = GameStatus.SCHEDULED
        log.info { "게임($gameId)에 대한 신청 승인 완료: applicantId=$applicantId" }
    }


    fun findParticipationId(gameId: Long, email: String): Long {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("게임 없음: $gameId") }
        val user = userRepository.findByEmail(email).orElseThrow{throw NoSuchElementException("유저 없음: $email")}

        val participation = participationRepository.findByGameAndUser(game, user)
            ?: throw NoSuchElementException("참여 신청 내역 없음")

        return participation.id
    }

//    fun getMyApprovedGames(): List<GameSummaryResponse> {
//        val user: UserEntity = authService.getCurrentUserEntity()
//        val now = LocalDateTime.now()
//
//        return participationRepository
//            .findAllByUserAndStatus(user, ParticipationStatus.APPROVED)
//            .map { it.game }
//            .filter { game ->
//                game.matchDate?.isAfter(now) ?: true
//            }
//            .map { it.toSummary() }
//    }

    fun getMyIncomingGames(user: UserEntity): List<MyGameResponse> {
        val participations = participationRepository.findAllByUserAndStatus(user, ParticipationStatus.APPROVED)

        return participations
            .map { it.game }
            /**
             * 진행 예정인 게임만 보여준다
             */
            .filter{
                it.gameStatus == GameStatus.SCHEDULED
            }
            .sortedByDescending { it.matchDate }
            .map { game ->
                val owner = game.createdBy
                val opponent = game.participations
                    .firstOrNull { it.user.id != owner.id }?.user

                MyGameResponse(
                    id = game.id,
                    matchLocation = game.placeRoad,
                    matchDate = game.matchDate,

                    majorCategory = game.rule.majorCategory,
                    minorCategory = game.rule.minorCategory,
                    ruleTitle = game.rule.ruleTitle,
                    ruleDescription = game.rule.ruleDescription,

                    ownerNickname = owner.nickname,
                    ownerProfileUrl = owner.profile?.profilePicture,

                    opponentId = opponent?.id ?: -1,
                    opponentNickname = opponent?.nickname ?: "대기 중",
                    opponentProfileUrl = opponent?.profile?.profilePicture,
                    opponentWins = 20,
                    opponentLosses = 4,
                    opponentDraws = 1,
                    opponentRating = 75.0,

                    status = game.gameStatus
                )
            }
    }


    private fun getGameOrThrow(id: Long): GameEntity =
        gameRepository.findWithRuleAndCreatedByById(id)
            ?: throw NoSuchElementException("게임 없음: $id")

    private fun getUserRef(id: Long) = userRepository.getReferenceById(id)

    private fun GameEntity.toSummary(): GameSummaryResponse =
        GameSummaryResponse(
            id = this.id,
            majorCategory = this.rule.majorCategory,
            minorCategory = this.rule.minorCategory,
            currentParticipantCounts = this.participations.count { it.status == ParticipationStatus.APPROVED },
            maxPlayers = this.maxPlayers,
            matchDate = this.matchDate,
            matchLocation = formatMatchLocation(this.placeRoad, this.placeDetail),
            createdAt = this.createdAt,
            ownerNickname = this.createdBy.nickname,
            ownerProfileUrl = this.createdBy.profile?.profilePicture,
            ruleTitle = this.rule.ruleTitle,
            ruleDescription = this.rule.ruleDescription,
            winCondition = this.rule.winBy.toString(),
            points = this.rule.pointsToWin,
            sets = this.rule.setsToWin,
            duration = this.rule.duration,
            gameStatus = this.gameStatus
        )

    private fun formatMatchLocation(road: String?, detail: String?): String? =
        listOfNotNull(road, detail).take(2).joinToString(" ")
}
