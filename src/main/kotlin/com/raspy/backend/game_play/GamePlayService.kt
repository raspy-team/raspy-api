package com.raspy.backend.game_play

import com.raspy.backend.chat.ChatRoomRepository
import com.raspy.backend.game.GameRepository
import com.raspy.backend.game.enumerated.GameStatus
import com.raspy.backend.game_play.request.ReviewRequest
import com.raspy.backend.game_play.response.GameDetailResponse
import com.raspy.backend.game_play.response.GameResultResponse
import com.raspy.backend.game_play.response.ScoreSummary
import com.raspy.backend.game_play.response.UserSummary
import com.raspy.backend.user.UserEntity
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class GamePlayService(
    private val gameRepository: GameRepository,
    private val reviewRepository: GameReviewRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val setLogRepository: SetLogRepository,
    private val scoreLogRepository: ScoreLogRepository
) {
    private val log = KotlinLogging.logger {}

    fun getDetail(id: Long, user: UserEntity): GameDetailResponse {
        val game = gameRepository.findByIdOrNull(id) ?: throw NoSuchElementException("게임 없음: $id")

        val users = game.participations.map { it.user }
        val (user1, user2) = users

        val latestSetLog: SetLogEntity = setLogRepository.findByGameIdOrderByStartedAtDesc(game.id)
        val currentSet = latestSetLog.totalSetIndex // 현재 진행 중인 세트 정보임
        /**
         * 현재 게임에서 각 유저가 획득한 세트, 스코어 연산
         */

        val scoreLogs: List<ScoreLogEntity> = scoreLogRepository.findAllByGame(game)
        val scoreLogsOfCurrentSet: List<ScoreLogEntity> =
            scoreLogs.filter{
                it.setIndex == currentSet
            }

        var user1Set = 0;
        var user2Set = 0;

        for(i in (1..currentSet-1)) {
            val set = scoreLogs.filter {
                it.setIndex == i
            }

            if(set.count{it.target == user1} > set.count{it.target == user2}) {
                user1Set += 1
            } else user2Set += 1
        }

        val user1Score = scoreLogsOfCurrentSet.count { it.target == user1 }
        val user2Score = scoreLogsOfCurrentSet.count { it.target == user2 }

        return GameDetailResponse(
            id = game.id,
            majorCategory = game.rule.majorCategory,
            minorCategory = game.rule.minorCategory,
            ruleTitle = game.rule.ruleTitle,
            place = formatMatchLocation(game.placeRoad, game.placeDetail),
            matchDate = game.matchDate,
            user1 = user1.toSummary(),
            user2 = user2.toSummary(),

            score1 = user1Score,
            score2 = user2Score,
            set1 = user1Set,
            set2 = user2Set,
            pointsToWin = game.rule.pointsToWin,
            setsToWin = game.rule.setsToWin,
            winBy = game.rule.winBy,
            limitSeconds = game.rule.duration,

            // 현재 세트가 시작된 시각을 의미함.
            setStartedAt = latestSetLog.startedAt,

            totalGameStartedAt = game.startedAt,
            // 게임이 존재한다면 채팅 방은 반드시 존재한다.
            chatRoomId = chatRoomRepository.findByGame(game)!!.id.toString(),
        )
    }

//    fun logScore(gameId: Long, actor: UserEntity, req: ScoreLogRequest) {
//        val game = gameRepository.findByIdOrNull(gameId) ?: throw NoSuchElementException("게임 없음")
//        val target = userRepository.findByIdOrNull(req.targetId) ?: throw NoSuchElementException("유저 없음")
//
//        val delta = if (req.action == "INCREMENT") 1 else -1
//        // TODO: 통계
//     //   game.score[target.id] = (game.score[target.id] ?: 0) + delta
//
//        val logEntry = ScoreLogEntity(
//            game = game,
//            actor = actor,
//            target = target,
//            action = req.action,
//            point = delta,
//            // TODO: 통계
//            setIndex = 1
//        )
//        scoreLogRepository.save(logEntry)
//
//        log.info { "점수 변경: game=$gameId, actor=${actor.id}, target=${target.id}, delta=$delta" }
//    }

//    fun moveToNextSet(gameId: Long) {
//      //  val game = gameRepository.findByIdOrNull(gameId) ?: throw NoSuchElementException("게임 없음")
//      //  game.currentSet += 1
//      //  log.info { "다음 세트 이동: game=$gameId, set=${game.currentSet}" }
//    }

    fun pauseGame(gameId: Long) {
        /**
         * TODO : 구현 예정
         */
        log.info { "게임 일시 정지: game=$gameId" }
    }

    fun finishGame(gameId: Long) {
        val game = gameRepository.findByIdOrNull(gameId) ?: throw NoSuchElementException("게임 없음")
        game.gameStatus = GameStatus.COMPLETED
    //    game.finishedAt = LocalDateTime.now()
        log.info { "게임 종료: game=$gameId" }
    }

    fun finishGame(roomId: UUID) {
        val room  = chatRoomRepository.findById(roomId).orElseThrow {throw Exception("존재하지 않는 게임을 종료하려 함")}
        finishGame(room.game!!.id)
    }

    /**
     * 게임이 취소 되는 것이 아니라, 게임 진행 상태는 유지된 채로 기록만 초기화하는 것임.
     */
    @Transactional
    fun resetGame(roomId: String) {
        val room  = chatRoomRepository.findById(UUID.fromString(roomId)).orElseThrow {throw Exception("존재하지 않는 게임을 리셋하려 함")}
        room.game!!.startedAt = LocalDateTime.now()

        scoreLogRepository.deleteAllByGameId(room.game.id)
        setLogRepository.deleteAllByGameId(room.game.id)

        setLogRepository.save(
            SetLogEntity(
                game = room.game,
                actor = room.game.createdBy,
                totalSetIndex = 1
            ))
    }

    fun getResult(id: Long): GameResultResponse {
        val game = gameRepository.findByIdOrNull(id) ?: throw NoSuchElementException("게임 없음")

        val (user1, user2) = game.participations.map { it.user }
//        val score1 = game.score[user1.id] ?: 0
//        val score2 = game.score[user2.id] ?: 0
        val score1 = 7
        val score2 = 8
    //    val winner = if (score1 > score2) user1 else user2
        val winner = user1
        return GameResultResponse(
            id = game.id,
            user1 = ScoreSummary(user1.nickname, user1.profile?.profilePicture, score1),
            user2 = ScoreSummary(user2.nickname, user2.profile?.profilePicture, score2),
            winner = winner.toSummary(),
            place = formatMatchLocation(game.placeRoad, game.placeDetail),
            majorCategory = game.rule.majorCategory,
            minorCategory = game.rule.minorCategory,
            ruleTitle = game.rule.ruleTitle,
            matchDate = game.matchDate ?: LocalDateTime.now()
        )
    }


    fun submitReview(gameId: Long, req: ReviewRequest) {
        val game = gameRepository.findByIdOrNull(gameId) ?: throw NoSuchElementException("게임 없음")
        val review = GameReviewEntity(
            game = game,
            manner = req.manner,
            performance = req.performance,
            text = req.text ?: "",
            createdAt = LocalDateTime.now()
        )
        reviewRepository.save(review)
        log.info { "리뷰 등록됨: game=$gameId manner=${req.manner}, performance=${req.performance}" }
    }

    private fun UserEntity.toSummary(): UserSummary = UserSummary(
        id = this.id!!,
        nickname = this.nickname,
        profileUrl = this.profile?.profilePicture
    )

    private fun formatMatchLocation(road: String?, detail: String?): String =
        listOfNotNull(road, detail).joinToString(" ")
}
