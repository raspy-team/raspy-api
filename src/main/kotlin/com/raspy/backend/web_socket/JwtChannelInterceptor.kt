package com.raspy.backend.web_socket

import com.raspy.backend.chat.ChatService
import com.raspy.backend.chat.MessageType
import com.raspy.backend.jwt.JwtUtil
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserService
import com.raspy.backend.web_socket.WebSocketConfig.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class JwtChannelInterceptor {
    class JwtChannelInterceptor(
        private val jwtUtil: JwtUtil,
        private val chatService: ChatService,
        private val userService: UserService
    ) : ChannelInterceptor {

        private val logger = KotlinLogging.logger {}

        companion object {
            /**
             * 중복 접속 방지를 위해 도입함.
             */
            private val activeUsers = ConcurrentHashMap<String, MutableMap<String, String>>() // roomId -> map<username, sessionId>
        }

        override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
            val accessor = StompHeaderAccessor.wrap(message)

            logger.info { "Active sessions: ${WebSocketSessionManager.sessionMap.size}" }
            logger.info { "Active users per room: ${activeUsers.mapValues { it.value.size }}" }

            if (StompCommand.CONNECT == accessor.command) {
                val token = accessor.getFirstNativeHeader("token")
                if (token.isNullOrBlank() || !jwtUtil.validateToken(token)) {
                    logger.warn { "Invalid or missing JWT on CONNECT" }
                    throw AccessDeniedException("Invalid JWT")
                }
                val authentication = jwtUtil.getAuthentication(token)
                accessor.sessionAttributes?.put("AUTHENTICATED_MEMBER_ID", authentication.name)
                logger.info { "CONNECT authenticated: ${authentication.name}" }
                return message
            }

            val user = accessor.sessionAttributes?.get("AUTHENTICATED_MEMBER_ID")?.toString()
                ?: throw AccessDeniedException("Missing AUTHENTICATED_MEMBER_ID")

            val userEntity: UserEntity = userService.getUserEntity(user)

            when (accessor.command) {

                StompCommand.SUBSCRIBE -> {
                    val roomId = accessor.destination?.removePrefix("/topic/ws/") ?: return message
                    val sessionId = accessor.sessionId ?: return message
                    val userMap = activeUsers.computeIfAbsent(roomId) { ConcurrentHashMap() }

                    chatService.notifyUserJoined(roomId, userEntity)
                    chatService.saveChatMessage(
                        roomId = roomId,
                        sender = userEntity,
                        content = "${userEntity.nickname}님이 입장하였습니다",
                        type = MessageType.ENTER
                    )

                    /**
                     * 방 중복입장 방지
                     */
                    val existingSessionId = userMap[user]
                    if (existingSessionId != null && existingSessionId != sessionId) {
                        logger.warn { "User $user already connected to $roomId with session $existingSessionId" }
                        // 기존 강제 세션 종료
                        WebSocketSessionManager.sessionMap[existingSessionId]?.close()
                    }
                    userMap[user] = sessionId
                }
                StompCommand.DISCONNECT -> {
                    // sessionId 기반으로 유저 삭제
                    activeUsers.forEach { (roomId, userMap) ->
                        val removed = userMap.entries.find { it.value == accessor.sessionId }
                        removed?.let { entry ->
                            userMap.remove(entry.key)
                            chatService.notifyUserLeft(roomId, userEntity)
                            chatService.saveChatMessage(
                                roomId = roomId,
                                sender = userEntity,
                                content = "${userEntity.nickname}님이 퇴장하였습니다",
                                type = MessageType.LEAVE
                            )
                            logger.info { "DISCONNECT: removed ${entry.key} from room $roomId" }
                        }
                    }
                }

                else -> {}
            }
            return message
        }
    }
}