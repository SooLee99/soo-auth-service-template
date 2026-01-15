package io.soo.springboot.core.domain

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.stereotype.Component

/**
 * ✅ 로그아웃 시
 * 1) user_session_map에 revoke(사유/시간 기록)
 * 2) Spring Session 저장소에서 세션 row 삭제 (즉시 무효화)
 */
@Component
class DBSessionRevokeLogoutHandler(
    private val sessionRepository: FindByIndexNameSessionRepository<out Session>,
    private val sessionMapService: io.soo.springboot.core.domain.UserSessionMapService,
) : LogoutHandler {

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?,
    ) {
        val session = request.getSession(false) ?: return
        val sid = session.id

        // ✅ 1) DB revoke 기록(멱등)
        // - reason은 로그아웃 호출 경로에 따라 다르게 줄 수도 있음
        sessionMapService.revoke(sessionId = sid, reason = "LOGOUT")

        // ✅ 2) Spring Session row 삭제(즉시 무효화)
        sessionRepository.deleteById(sid)
    }
}
