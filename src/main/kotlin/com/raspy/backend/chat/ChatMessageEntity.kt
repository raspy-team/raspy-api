package com.raspy.backend.chat

import com.raspy.backend.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_message")
data class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    val chatRoom: ChatRoomEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: UserEntity,

    @Column(nullable = false, length = 1000)
    val message: String,

    @Column(nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now()
)