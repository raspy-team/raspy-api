package com.raspy.backend.chat

import com.raspy.backend.game_play.ScoreLogEntity
import com.raspy.backend.game_play.ScoreLogRepository
import com.raspy.backend.game_play.SetLogEntity
import com.raspy.backend.game_play.SetLogRepository
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserRepository
import com.raspy.backend.web_socket.GameWs.ScoreUpdate
import mu.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val scoreLogRepository: ScoreLogRepository,
    private val setLogRepository: SetLogRepository
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
    fun saveScoreLog(
        update: ScoreUpdate,
        roomId: String,
        actor: UserEntity,
    ): ScoreLogEntity {
        log.info { "Saving chat message start about scoring" }
        val chatRoom = chatRoomRepository.getReferenceById(UUID.fromString(roomId))

        val scoreLog = ScoreLogEntity(
            game = chatRoom.game!!,
            actor = actor,
            target = userRepository.findById(update.userId).orElseThrow { throw RuntimeException("not exist target user") },
            scoreDelta = update.scoreDelta,
            setIndex = update.setIndex,
        )
        scoreLogRepository.save(scoreLog)

        log.info { "Saved ${update.type} scoring message in room $roomId" }
        return scoreLog
    }


    fun saveStartSetLog(
        newSet: Int,
        roomId: String,
        actor: UserEntity
    ): SetLogEntity{
        log.info { "Saving chat message start about set" }
        val chatRoom = chatRoomRepository.getReferenceById(UUID.fromString(roomId))

        return setLogRepository.save(SetLogEntity(
            game = chatRoom.game!!,
            actor = actor,
            totalSetIndex = newSet,
        ))
    }


}
