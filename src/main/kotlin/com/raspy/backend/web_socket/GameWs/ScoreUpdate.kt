package com.raspy.backend.web_socket.GameWs


data class ScoreUpdate(
    val type: String,          // "SCORE", "SET", "FINISH"
    val userId: Long? = null,  // SCORE 타입일 경우 필수
    val delta: Int? = null,    // SCORE 타입일 경우 필수
    val set: Int? = null       // SET 타입일 경우 필수
)
