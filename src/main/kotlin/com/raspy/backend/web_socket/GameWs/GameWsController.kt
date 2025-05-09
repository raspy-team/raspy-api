package com.raspy.backend.web_socket.GameWs

import com.raspy.backend.web_socket.ChatMessage
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class GameWsController {

    private val logger = KotlinLogging.logger {}

    @Operation(
        summary = "채팅 전송",
        description = "유저가 지정한 방으로 채팅 메시지를 전송합니다"
    )
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    fun handleChat(
        @DestinationVariable roomId: String,
        message: ChatMessage
    ): ChatMessage {
        logger.info { "Room[$roomId] ${message.sender} → ${message.content}" }
        return message
    }

    @Operation(
        summary = "점수 업데이트",
        description = "유저가 지정한 방의 점수를 증가/감소시킵니다"
    )
    @MessageMapping("/score/{roomId}")
    @SendTo("/topic/score/{roomId}")
    fun handleScore(
        @DestinationVariable roomId: String,
        update: ScoreUpdate
    ): ScoreUpdate {
        logger.info { "Room[$roomId] ${update.userId} 점수변경: Δ=${update.delta}" }
        return update
    }
}