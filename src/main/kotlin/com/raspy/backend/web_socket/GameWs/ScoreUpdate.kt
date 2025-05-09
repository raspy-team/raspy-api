package com.raspy.backend.web_socket.GameWs


data class ScoreUpdate(
    val userId: String,
    val delta: Int,  // 증가(+), 감소(–) 값
    val timestamp: Long
)