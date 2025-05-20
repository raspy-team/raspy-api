package com.raspy.backend.comment.response

import com.raspy.backend.comment.CommentEntity
import java.time.LocalDateTime


data class CommentResponse(
    val id: Long,
    val authorNickname: String,
    val authorProfileUrl: String?,
    val content: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: CommentEntity): CommentResponse = CommentResponse(
            id = entity.id,
            authorNickname = entity.author.nickname,
            authorProfileUrl = entity.author.profile?.profilePicture,
            content = entity.content,
            createdAt = entity.createdAt
        )
    }
}
