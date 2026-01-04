package io.soo.springboot.core.domain

import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ForceLogoutService(
    private val sessionRepository: FindByIndexNameSessionRepository<out Session>,
    private val sessionMapService: UserSessionMapService,
) {
    @Transactional
    fun forceLogout(userId: Long, deviceId: String? = null, reason: String? = "FORCED_LOGOUT") {
        val sessionIds = sessionMapService.activeSessionIds(userId, deviceId)
        sessionIds.forEach { sid ->
            // 1) Spring Session JDBC row 삭제 => 즉시 로그아웃
            sessionRepository.deleteById(sid)
            // 2) 매핑 테이블 revoke 표시
            sessionMapService.revokeSession(sid, reason)
        }
    }
}
