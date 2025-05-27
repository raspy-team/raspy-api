package com.raspy.backend.web_socket.GameWs

import com.raspy.backend.chat.ChatService
import com.raspy.backend.chat.MessageType
import com.raspy.backend.game.GameService
import com.raspy.backend.game_play.GamePlayService
import com.raspy.backend.user.UserService
import com.raspy.backend.web_socket.ChatMessage
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class GameWsController(
    private val chatService: ChatService,
    private val userService: UserService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val gameService: GameService,
    private val gamePlayService: GamePlayService,

    ) {

    private val logger = KotlinLogging.logger {}

    data class Message(val content: String)

    @Operation(
        summary = "채팅 전송",
        description = "유저가 지정한 방으로 채팅 메시지를 전송합니다"
    )
    @MessageMapping("/chat/{roomId}")
    fun handleChat(
        @DestinationVariable roomId: String,
        message: Message,
        @Header("simpSessionAttributes") sessionAttrs: Map<String, Any>
    ): Unit {
        val userId = sessionAttrs["AUTHENTICATED_MEMBER_ID"] as? String
            ?: throw AccessDeniedException("WebSocket 인증 실패: 세션에 유저 ID 없음")

        val user = userService.getUserEntity(userId)

        val messageEntity = chatService.saveChatMessage(roomId, user, message.content, MessageType.TALK)

        logger.info { "Room[$roomId] ${user.email} → $message" }

        messagingTemplate.convertAndSend(
            "/topic/ws/$roomId",
            ChatMessage(
                sender = messageEntity.sender.nickname,
                content = messageEntity.message,
                timestamp = messageEntity.sentAt,
                senderId = messageEntity.sender.id!!,
                messageType = messageEntity.type
            )
        )
    }


    @Operation(
        summary = "점수 및 게임 상태 이벤트 처리",
        description = "점수 증감, 세트 변경, 경기 종료 등의 실시간 이벤트를 처리합니다."
    )
    @MessageMapping("/score/{roomId}")
    fun handleGameEvent(
        @DestinationVariable roomId: String,
        update: ScoreUpdate,
        @Header("simpSessionAttributes") sessionAttrs: Map<String, Any>
    ) {
        val userId = sessionAttrs["AUTHENTICATED_MEMBER_ID"] as? String
            ?: throw AccessDeniedException("WebSocket 인증 실패: 세션에 유저 ID 없음")

        val actor = userService.getUserEntity(userId)

        when (update.type) {
            "SCORE" -> {
                /**
                 * TODO: 스코어가 음수가 되는 경우
                 */
                if (update.userId == null || update.scoreDelta == null)
                    throw IllegalArgumentException("SCORE 이벤트에는 userId와 delta가 필요합니다.")

                chatService.saveScoreLog(
                    update,
                    roomId,
                    actor
                )

                // 클라이언트로 전파
                messagingTemplate.convertAndSend(
                    "/topic/ws/$roomId",
                    mapOf(
                        "type" to "SCORE",
                        "userId" to update.userId,
                        "delta" to update.scoreDelta
                    )
                )

                logger.info { "Room[$roomId] 점수 변경: userId=${update.userId}, Δ=${update.scoreDelta}" }
            }

            "SET" -> {
                chatService.saveStartSetLog(
                    update.setIndex,
                    roomId,
                    actor
                )


                messagingTemplate.convertAndSend(
                    "/topic/ws/$roomId",
                    mapOf(
                        "type" to "SET",
                        // "set" to update.set + 1
                    )
                )

                logger.info { "Room[$roomId] 세트 변경 → ${update.setIndex}" }
            }

            "FINISH" -> {
                gamePlayService.finishGame(UUID.fromString(roomId));

                messagingTemplate.convertAndSend(
                    "/topic/ws/$roomId",
                    mapOf(
                        "type" to "FINISH"
                    )
                )

                logger.info { "Room[$roomId] 경기 종료 처리됨" }
            }

            "RESET" -> {
                gamePlayService.resetGame(roomId)
            }

            else -> throw IllegalArgumentException("지원하지 않는 타입입니다: ${update.type}")
        }
    }
}