package com.raspy.backend.chat

import com.raspy.backend.game.GameEntity
import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "chat_room")
data class ChatRoomEntity(

    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ChatRoomType,  // GAME or DM

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // [GAME] 채팅방일 경우 연결되는 게임
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", unique = true)
    val game: GameEntity? = null,

    // [DM] 채팅방일 경우 참여 유저 2명
    @ManyToMany
    @JoinTable(
        name = "chat_room_user",
        joinColumns = [JoinColumn(name = "chat_room_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val dmParticipants: Set<UserEntity> = emptySet()  // DM only
) {
    fun getParticipants(): Set<UserEntity> {
        return when (type) {
            ChatRoomType.GAME -> game!!.participants
            ChatRoomType.DM -> dmParticipants
        }
    }
}
