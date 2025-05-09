package com.raspy.backend.web_socket

import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent

@Component
class PresenceListener {
    private val logger = KotlinLogging.logger {}

    @EventListener
    fun handleSubscribe(event: SessionSubscribeEvent) {
        val dest = event.message.headers["simpDestination"]
        logger.info { "새 구독: $dest" }
    }
}
