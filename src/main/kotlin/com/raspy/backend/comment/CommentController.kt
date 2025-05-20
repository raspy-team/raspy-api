package com.raspy.backend.comment

import com.raspy.backend.auth.AuthService
import com.raspy.backend.comment.request.CreateCommentRequest
import com.raspy.backend.comment.response.CommentResponse
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games/{gameId}/comments")
class CommentController(
    private val commentService: CommentService,
    private val authService: AuthService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping
    @Operation(summary = "Get Comments", description = "게임 ID에 해당하는 모든 댓글을 조회합니다.")
    fun getComments(@PathVariable gameId: Long): ResponseEntity<List<CommentResponse>> {
        return ResponseEntity.ok(commentService.getComments(gameId))
    }

    @PostMapping
    @Operation(summary = "Add Comment", description = "특정 게임에 댓글을 작성합니다.")
    fun addComment(
        @PathVariable gameId: Long,
        @RequestBody req: CreateCommentRequest
    ): ResponseEntity<Unit> {
        val user = authService.getCurrentUserEntity()
        commentService.addComment(gameId, user, req)
        return ResponseEntity.ok().build()
    }
}