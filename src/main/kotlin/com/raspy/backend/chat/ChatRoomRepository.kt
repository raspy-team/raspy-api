package com.raspy.backend.chat

import com.raspy.backend.game.GameEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ChatRoomRepository: JpaRepository<ChatRoomEntity, UUID> {
    fun findByGame(game: GameEntity): ChatRoomEntity?
}