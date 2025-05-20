package com.raspy.backend.game.enumerated

// TODO: 스케쥴러로 경기일시가 지났는데 진행되지 못한 게임은 CANCELED로 변경해야함
enum class GameStatus {
    MATCHING,       // 상대방 구하는 중
    SCHEDULED,      // 시작 예정
    IN_PROGRESS,    // 진행 중
    COMPLETED,      // 종료됨
    CANCELED,       // 취소됨 (사용자 또는 관리자에 의해)
    DELETED         // 삭제됨 (DB에선 남아있지만 사용자에겐 숨김)
}