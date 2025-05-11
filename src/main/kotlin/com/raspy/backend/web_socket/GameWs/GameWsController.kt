package com.raspy.backend.web_socket.GameWs

import com.raspy.backend.auth.AuthService
import com.raspy.backend.chat.ChatService
import com.raspy.backend.chat.MessageType
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserService
import com.raspy.backend.web_socket.ChatMessage
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller

@Controller
class GameWsController(
    private val chatService: ChatService,
    private val userService: UserService,
    private val messagingTemplate: SimpMessagingTemplate,

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
                timestamp = messageEntity.sentAt
            )
        )
    }


    @Operation(
        summary = "점수 업데이트",
        description = "유저가 지정한 방의 점수를 증가/감소시킵니다"
    )
    @MessageMapping("/score/{roomId}")
    @SendTo("/topic/ws/{roomId}")
    fun handleScore(
        @DestinationVariable roomId: String,
        update: ScoreUpdate
    ): ScoreUpdate {
        logger.info { "Room[$roomId] ${update.userId} 점수변경: Δ=${update.delta}" }

        /**
         *  SCORING 저장 필요
         */

        return update
    }
}