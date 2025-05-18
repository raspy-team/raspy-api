//package com.raspy.backend.game
//
//import com.raspy.backend.auth.AuthService
//import com.raspy.backend.game.enumerated.WinCondition
//import com.raspy.backend.game.request.CreateGameRequest
//import com.raspy.backend.game.response.GameSummaryResponse
//import com.raspy.backend.jwt.UserPrincipal
//import com.raspy.backend.user.enumerated.Role
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito
//import org.mockito.Mockito.verify
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.http.MediaType
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//import java.time.LocalDateTime
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class GameControllerTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockBean
//    private lateinit var gameService: GameService
//
//    @MockBean
//    private lateinit var authService: AuthService
//
//    // 테스트용 UserPrincipal을 AuthService가 리턴하도록 설정
//    private val principal = UserPrincipal(
//        id = 1L,
//        email = "u@e.com",
//        password = "pw",
//        authorities = listOf(Role.ROLE_USER.toGrantedAuthority())
//    )
//
//    @Test
//    @WithMockUser
//    fun `게임 생성 성공 - 정상 요청`() {
//        // AuthService.getCurrentUser()가 principal을 리턴
//        Mockito.`when`(authService.getCurrentUser()).thenReturn(principal)
//
//        val json = """
//          {
//            "title":"Standard Game",
//            "description":"A normal game",
//            "rulesDescription":"Standard rules",
//            "majorCategory":"Ball",
//            "minorCategory":"Soccer",
//            "pointsToWin":10,
//            "setsToWin":2,
//            "duration":900,
//            "winBy":"SETS_HALF_WIN",
//            "matchDate":"2025-05-10T14:00:00",
//            "placeRoad":"Main St",
//            "placeDetail":"Field 1"
//          }
//        """.trimIndent()
//
//        mockMvc.perform(
//            post("/api/games/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json)
//        )
//            .andExpect(status().isOk)
//            .andExpect(content().string("game is created"))
//
//        verify(gameService).createGame(
//            CreateGameRequest(
//                title            = "Standard Game",
//                description      = "A normal game",
//                rulesDescription = "Standard rules",
//                majorCategory    = "Ball",
//                minorCategory    = "Soccer",
//                pointsToWin      = 10,
//                setsToWin        = 2,
//                duration         = 900,
//                winBy            = WinCondition.SETS_HALF_WIN.name,
//                matchDate        = LocalDateTime.parse("2025-05-10T14:00:00"),
//                placeRoad        = "Main St",
//                placeDetail      = "Field 1",
//            ),
//            1L
//        )
//    }
//
//    @Test
//    @WithMockUser
//    fun `게임 생성 실패 - title 누락`() {
//        Mockito.`when`(authService.getCurrentUser()).thenReturn(principal)
//
//        val json = """
//          {
//            "description":"No title",
//            "rulesDescription":"Rules",
//            "majorCategory":"Ball",
//            "minorCategory":"Soccer",
//            "pointsToWin":5,
//            "setsToWin":1,
//            "duration":300,
//            "winBy":"SETS_HALF_WIN"
//          }
//        """.trimIndent()
//
//        mockMvc.perform(
//            post("/api/games/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json)
//        )
//            .andExpect(status().isBadRequest)
//    }
//
//    @Test
//    fun `게임 생성 실패 - 인증 없음`() {
//        // AuthService는 stub 해두지 않으므로 실제로 인증 필터가 작동해 403
//        val json = """
//          {
//            "title":"NoAuth",
//            "description":"No auth",
//            "rulesDescription":"Rules",
//            "majorCategory":"Any",
//            "minorCategory":"Any",
//            "pointsToWin":5,
//            "setsToWin":1,
//            "duration":300,
//            "winBy":"SETS_HALF_WIN"
//          }
//        """.trimIndent()
//
//        mockMvc.perform(
//            post("/api/games/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json)
//        )
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    @WithMockUser
//    fun `게임 리스트 조회 성공`() {
//        // 테스트용 게임 목록 반환 설정
//        val gameList = listOf(
//            GameSummaryResponse(
//                id = 1L,
//                majorCategory = "Ball",
//                minorCategory = "Soccer",
//                currentParticipantCounts = 5,
//                maxPlayers = 10,
//                matchDate = null,
//                matchLocation = "경기도 수원시 영통동 일이삼 1"
//            ),
//            GameSummaryResponse(
//                id = 2L,
//                title = "Game 2",
//                majorCategory = "Board",
//                minorCategory = "Chess",
//                description = "Second game description",
//                currentParticipantCounts = 2,
//                maxPlayers = 2,
//                matchDate = null,
//                matchLocation = "부산시 금정구 빵빵동 1"
//            )
//        )
//
//        Mockito.`when`(gameService.findAllSummaries()).thenReturn(gameList)
//
//        mockMvc.perform(
//            get("/api/games")
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$[0].title").value("Game 1"))
//            .andExpect(jsonPath("$[1].title").value("Game 2"))
//    }
//
//    @Test
//    @WithMockUser
//    fun `게임 참가 성공`() {
//        Mockito.`when`(authService.getCurrentUser()).thenReturn(principal)
//
//        mockMvc.perform(
//            post("/api/games/42/join")
//        )
//            .andExpect(status().isOk)
//
//        verify(gameService).joinGame(42L, principal.id)
//    }
//
//    @Test
//    fun `게임 참가 실패 - 인증 없음`() {
//        mockMvc.perform(
//            post("/api/games/42/join")
//        )
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    @WithMockUser
//    fun `게임 나가기 성공`() {
//        Mockito.`when`(authService.getCurrentUser()).thenReturn(principal)
//
//        mockMvc.perform(
//            delete("/api/games/42/leave")
//        )
//            .andExpect(status().isOk)
//
//        verify(gameService).leaveGame(42L, principal.id)
//    }
//
//    // 🔹 게임 나가기 실패—인증 없음 → 403
//    @Test
//    fun `게임 나가기 실패 - 인증 없음`() {
//        mockMvc.perform(
//            delete("/api/games/42/leave")
//        )
//            .andExpect(status().isForbidden)
//    }
//}
