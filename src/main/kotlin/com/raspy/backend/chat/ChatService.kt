package com.raspy.backend.chat

import com.raspy.backend.jwt.UserPrincipal
import com.raspy.backend.user.UserEntity
import mu.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.LocalDateTime
import java.util.*

@Service
class ChatService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository
) {
    private val log = KotlinLogging.logger {}

    /**
     * 게임 채팅방에 유저 입장 알림을 브로드캐스트합니다.
     * @param roomId  대상 게임 ID
     * @param principal  WebSocket 연결 시 SecurityContext 에 올라온 UserPrincipal
     */
    fun notifyUserJoined(roomId: String, user: UserEntity) {
        val info = ChatStatusChangeMessage(
            sender = user.nickname,
            content = "${user.nickname}님이 입장하였습니다." ,
        )

        // /topic/chat/{roomId}/join 으로 입장 DTO 전송
        messagingTemplate.convertAndSend("/topic/ws/$roomId", info)
        log.info { "User ${user.id}(${user.nickname}) joined room $roomId " }
    }

    fun notifyUserLeft(roomId: String, user: UserEntity) {
        val info = ChatStatusChangeMessage(
            sender = user.nickname,
            content = "${user.nickname}님이 퇴장하였습니다." ,
        )

        messagingTemplate.convertAndSend("/topic/ws/$roomId", info)
        log.info { "User ${user.id}(${user.nickname}) left room $roomId" }
    }


    /**
     * Dm 관련 메시지 처리
     */
    fun saveChatMessage(
        roomId: String,
        sender: UserEntity,
        content: String,
        type: MessageType
    ): ChatMessageEntity {
        log.info { "Saving chat message start" }
        val chatRoom = chatRoomRepository.getReferenceById(UUID.fromString(roomId))

        val message = ChatMessageEntity(
                chatRoom = chatRoom,
                sender = sender,
                message = content,
                type = type
        )
        chatMessageRepository.save(message)
        log.info { "Saved $type message from ${sender.id} in room $roomId" }
        return message
    }

    /**
     * 점수 관련 메시지 처리
     */
    fun saveChatMessage(
        roomId: String,
        sender: UserEntity,
        scoreDelta: Int,
        type: MessageType
    ): ChatMessageEntity {
        log.info { "Saving chat message start about scoring" }
        val chatRoom = chatRoomRepository.getReferenceById(UUID.fromString(roomId))

        /**
         * TODO : 스코어 계산해서 다음 세트로 가야하는지, 말아야하는지 결정해야함.
         */
        val message = ChatMessageEntity(
            chatRoom = chatRoom,
            sender = sender,
            scoreDelta = scoreDelta,
            type = type
        )
        chatMessageRepository.save(message)
        log.info { "Saved $type scoring message from ${sender.id} in room $roomId" }
        return message
    }



}
