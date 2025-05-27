package com.raspy.backend.web_socket.GameWs


data class ScoreUpdate(
    val type: String,          // "SCORE", "SET", "FINISH"

    val userId: Long,       // SCORE 타입일 경우 필수
    val scoreDelta: Int,    // SCORE 타입일 경우 필수
    val setIndex: Int,      // SCORE 타입일 경우 필수 (몇 세트에서의 득실점인지)
)
