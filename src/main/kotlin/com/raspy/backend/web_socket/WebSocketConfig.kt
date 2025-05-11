package com.raspy.backend.web_socket

import com.raspy.backend.chat.ChatService
import com.raspy.backend.jwt.JwtUtil
import com.raspy.backend.user.UserService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtUtil: JwtUtil,
    @Lazy private val chatService: ChatService,
    private val userService: UserService,


    ) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:8081","http://localhost:8000")
            .addInterceptors(HttpSessionHandshakeInterceptor())
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(JwtChannelInterceptor.JwtChannelInterceptor(jwtUtil, chatService, userService))
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


}