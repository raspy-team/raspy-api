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
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
   // private val jwtUtil: JwtUtil
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
        // 클라이언트가 메시지를 보낼 때 prefix로 "/app"을 사용하도록 지정
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            // 클라이언트가 WS 핸드셰이크 시도할 엔드포인트를 "/ws"로 노출
            .setAllowedOrigins("http://localhost:5500", "http://localhost:8000")
            .withSockJS() // SockJS 폴백을 활성화해, WebSocket 미지원 브라우저 대응
    }

}