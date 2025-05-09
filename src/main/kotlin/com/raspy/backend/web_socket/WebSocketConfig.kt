package com.raspy.backend.web_socket

import com.raspy.backend.jwt.JwtUtil
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

import java.security.Principal
import java.util.concurrent.ConcurrentHashMap
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtUtil: JwtUtil
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .addInterceptors(HttpSessionHandshakeInterceptor())
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(JwtChannelInterceptor(jwtUtil))
    }

    override fun configureWebSocketTransport(registration: WebSocketTransportRegistration) {
        registration.addDecoratorFactory(WebSocketSessionManager().decoratorFactory())
    }

    @Configuration
    class WebSocketSessionManager {
        companion object {
            val sessionMap: MutableMap<String, WebSocketSession> = ConcurrentHashMap()
        }

        fun decoratorFactory(): WebSocketHandlerDecoratorFactory =
            WebSocketHandlerDecoratorFactory { handler ->
                object : WebSocketHandlerDecorator(handler) {
                    override fun afterConnectionEstablished(session: WebSocketSession) {
                        sessionMap[session.id] = session
                        super.afterConnectionEstablished(session)
                    }

                    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: org.springframework.web.socket.CloseStatus) {
                        sessionMap.remove(session.id)
                        super.afterConnectionClosed(session, closeStatus)
                    }
                }
            }
    }

    private class JwtChannelInterceptor(
        private val jwtUtil: JwtUtil
    ) : ChannelInterceptor {

        private val logger = KotlinLogging.logger {}

        companion object {
            private val activeUsers = ConcurrentHashMap<String, MutableMap<String, String>>() // roomId -> map<username, sessionId>
        }

        override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
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

            when (accessor.command) {
                StompCommand.SUBSCRIBE -> {
                    val roomId = accessor.destination?.removePrefix("/topic/chat/") ?: return message
                    val sessionId = accessor.sessionId ?: return message
                    val userMap = activeUsers.computeIfAbsent(roomId) { ConcurrentHashMap() }

                    val existingSessionId = userMap[user]
                    if (existingSessionId != null && existingSessionId != sessionId) {
                        logger.warn { "User $user already connected to $roomId with session $existingSessionId" }
                        // 기존 강제 세션 종료, 새 세션 대입
                        WebSocketSessionManager.sessionMap[existingSessionId]?.close()
                        userMap[user] = sessionId
                    } else {
                        userMap[user] = sessionId
                    }
                }
                StompCommand.DISCONNECT -> {
                    activeUsers.values.forEach { it.remove(user) }
                    logger.info { "DISCONNECT: removed $user from all rooms" }
                }
                else -> {}
            }
            return message
        }
    }
}