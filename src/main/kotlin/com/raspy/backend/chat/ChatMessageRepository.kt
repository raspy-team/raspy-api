package com.raspy.backend.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository: JpaRepository<ChatMessageEntity, Long> {
    fun findAllByChatRoomOrderBySentAtAsc(chatRoom: ChatRoomEntity): List<ChatMessageEntity>

}