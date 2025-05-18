package com.raspy.backend.game

import com.raspy.backend.auth.AuthService
import com.raspy.backend.chat.ChatRoomRepository
import com.raspy.backend.game.enumerated.ParticipationStatus
import com.raspy.backend.rule.RuleEntity
import com.raspy.backend.rule.RuleRepository
import com.raspy.backend.user.UserEntity
import com.raspy.backend.user.UserRepository
import com.raspy.backend.user.UserService
import com.raspy.backend.game.enumerated.WinCondition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class GameServiceUnitTest {

    @InjectMocks
    private lateinit var gameService: GameService

    @Mock private lateinit var gameRepository: GameRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var authService: AuthService
    @Mock private lateinit var chatRoomRepository: ChatRoomRepository
    @Mock private lateinit var participationRepository: ParticipationRepository
    @Mock private lateinit var ruleRepository: RuleRepository
    @Mock private lateinit var userService: UserService

    private lateinit var host: UserEntity
    private lateinit var user: UserEntity
    private lateinit var rule: RuleEntity
    private lateinit var game: GameEntity

    @BeforeEach
    fun setup() {
        host = UserEntity(id = 10L, email = "host@test.com", nickname = "host", password = "1234")
        user = UserEntity(id = 1L, email = "user@test.com", nickname = "user", password = "abcd")

        rule = RuleEntity(
            ruleTitle = "test",
            ruleDescription = "desc",
            majorCategory = "sports",
            minorCategory = "soccer",
            pointsToWin = 5,
            setsToWin = 3,
            duration = 300,
            winBy = WinCondition.MOST_SETS_AND_POINTS,
            createdBy = "host@test.com"
        )

        game = GameEntity(
            id = 100L,
            rule = rule,
            matchDate = LocalDateTime.now().plusDays(1),
            placeRoad = "서울시",
            placeDetail = "1층",
            createdBy = host,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `applyToJoinGame - 정상 신청`() {
        // given
        `when`(authService.getCurrentUserEntity()).thenReturn(user)
        `when`(gameRepository.findWithRuleAndCreatedByById(100L)).thenReturn(game)
        `when`(participationRepository.existsByGameAndUser(game, user)).thenReturn(false)

        // when
        gameService.applyToJoinGame(100L)

        // then
        verify(participationRepository).save(
            argThat {
                it.game == game &&
                        it.user == user &&
                        it.status == ParticipationStatus.REQUESTED
            }
        )
    }

    @Test
    fun `applyToJoinGame - 자기 게임 신청 예외`() {
        // given
        `when`(authService.getCurrentUserEntity()).thenReturn(host)
        `when`(gameRepository.findWithRuleAndCreatedByById(100L)).thenReturn(game)

        // expect
        val exception = assertThrows<Exception> {
            gameService.applyToJoinGame(100L)
        }
            assertThat(exception.message).contains("자신이 생성한 게임에는 신청할 수 없습니다")
    }

    @Test
    fun `applyToJoinGame - 이미 신청한 게임 예외`() {
        // given
        `when`(authService.getCurrentUserEntity()).thenReturn(user)
        `when`(gameRepository.findWithRuleAndCreatedByById(100L)).thenReturn(game)
        `when`(participationRepository.existsByGameAndUser(game, user)).thenReturn(true)

        // expect
        val exception = assertThrows<Exception> {
            gameService.applyToJoinGame(100L)
        }
        assertThat(exception.message).contains("이미 신청한 게임입니다")
    }
}
