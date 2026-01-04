package io.soo.springboot.core.domain

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class DevicePolicyFilter(
    private val sessionMapService: UserSessionMapService,
    private val deviceBlockService: DeviceBlockService,
    private val sessionRepository: FindByIndexNameSessionRepository<out Session>
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val session = req.getSession(false)
        if (session == null) {
            chain.doFilter(req, res); return
        }

        val sessionId = session.id
        val mapping = sessionMapService.findActive(sessionId)
        if (mapping == null) {
            sessionRepository.deleteById(sessionId)
            res.sendError(401, "Session revoked")
            return
        }

        if (deviceBlockService.isBlocked(mapping.userId, mapping.deviceId)) {
            sessionRepository.deleteById(sessionId)
            res.sendError(403, "Blocked device")
            return
        }

        chain.doFilter(req, res)
    }
}
