package com.raspy.backend.chat

import com.raspy.backend.game.GameRepository
import com.raspy.backend.game.GameEntity
import com.raspy.backend.game.ParticipationEntity
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserRepository
import com.raspy.backend.web_socket.ChatMessage
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/chat-room")
class ChatRoomController(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{roomId}/chat-messages")
    fun getChatMessages(@PathVariable roomId: UUID): List<ChatMessage> {
        val chatRoom = chatRoomRepository.findById(roomId).orElseThrow {
            NoSuchElementException("채팅방이 존재하지 않습니다: $roomId")
        }
        val messages = chatMessageRepository.findAllByChatRoomOrderBySentAtAsc(chatRoom)
        return messages.map {
            ChatMessage(
                sender = it.sender.nickname,
                content = it.message,
                timestamp = it.sentAt,
                senderId = it.sender.id!!,
                messageType = it.type
            )
        }
    }


    @GetMapping("/by-game/{gameId}")
    fun getChatRoomIdByGameId(@PathVariable gameId: Long): Map<String, UUID> {
        val game = gameRepository.findById(gameId).orElseThrow {
            NoSuchElementException("게임을 찾을 수 없습니다: $gameId")
        }

        val chatRoom = chatRoomRepository.findByGame(game)
            ?: throw NoSuchElementException("채팅방이 존재하지 않습니다: gameId=$gameId")

        return mapOf("roomId" to chatRoom.id)
    }

    @PostConstruct
    fun initDefaultGameAndChatRoom() {
        userRepository.save(
            UserEntity(
                email = "hahaha123@gmail.com",
                password = "123123123",
                nickname = "테스트맨",
            )
        )
        if (gameRepository.count() == 0L) {
            val game = GameEntity(
                rule = com.raspy.backend.game.RuleEntity(
                    ruleTitle = "에이아이가 만든 타이틀",
                    majorCategory = "축구",
                    minorCategory = "풋살",
                    pointsToWin = 3,
                    setsToWin = 1,
                    duration = 600,
                    winBy = com.raspy.backend.game.enumerated.WinCondition.SETS_HALF_WIN,
                    ruleDescription = "내가설명햇자나!!",
                    createdBy = "hahaha123@gmail.com",
                ),
                matchDate = null,
                placeRoad = "서울특별시 강남구",
                placeDetail = "어디든지",
                createdBy = userRepository.getReferenceById(1),
                createdAt = java.time.LocalDateTime.now(),
            )
            val savedGame = gameRepository.save(game)
            chatRoomRepository.save(
                ChatRoomEntity(
                    type = ChatRoomType.GAME,
                    game = savedGame
                )
            )
            log.info { "기본 게임 및 채팅방이 생성되었습니다. gameId=${savedGame.id}" }
        }
    }
}