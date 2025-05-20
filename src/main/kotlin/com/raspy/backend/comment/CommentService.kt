package com.raspy.backend.comment

import com.raspy.backend.comment.request.CreateCommentRequest
import com.raspy.backend.comment.response.CommentResponse
import com.raspy.backend.game.GameRepository
import com.raspy.backend.user.UserEntity
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService(
    private val gameRepository: GameRepository,
    private val commentRepository: CommentRepository
) {
    private val log = KotlinLogging.logger {}

    fun getComments(gameId: Long): List<CommentResponse> {
        return commentRepository.findByGameIdOrderByCreatedAtDesc(gameId)
            .map { CommentResponse.from(it) }
    }

    fun addComment(gameId: Long, user: UserEntity, req: CreateCommentRequest) {
        val game = gameRepository.findByIdOrNull(gameId) ?: throw NoSuchElementException("게임이 존재하지 않음")
        val comment = CommentEntity(
            game = game,
            author = user,
            content = req.content,
            createdAt = LocalDateTime.now()
        )

        log.info{"댓글 추가 됨 : ${req.content}"}
        commentRepository.save(comment)
    }
}
