package io.soo.springboot.core.domain

import io.soo.springboot.core.api.controller.v1.response.AdminSessionDto
import io.soo.springboot.storage.db.core.UserSessionMapRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminSessionQueryService(
    private val userSessionMapRepository: UserSessionMapRepository,
) {

    /**
     * ✅ 유저 세션 목록 조회
     * - revokedOnly / activeOnly 둘 중 하나만 쓰는 걸 권장(동시 true면 혼란)
     * - deviceId로 추가 필터 가능
     */
    @Transactional(readOnly = true)
    fun listUserSessions(
        userId: Long,
        deviceId: String?,
        activeOnly: Boolean,
    ): List<AdminSessionDto> {
        val sessions = when {
            !deviceId.isNullOrBlank() -> userSessionMapRepository.findAllByUserIdAndDeviceIdOrderByCreatedAtDesc(userId, deviceId)
            activeOnly -> userSessionMapRepository.findAllByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId)
            else -> userSessionMapRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        }

        return sessions.map {
            it.id?.let { id ->
                it.createdAt?.let { createdAt ->
                    AdminSessionDto(
                        id = id,
                        sessionId = it.sessionId,
                        userId = it.userId,
                        deviceId = it.deviceId,
                        provider = it.provider,
                        createdAt = createdAt,
                        lastAccessedAt = it.lastAccessedAt,
                        revokedAt = it.revokedAt,
                        revokedReason = it.revokedReason,
                    )
                }
            } as AdminSessionDto
        }
    }

    /**
     * ✅ 세션 단건 상세 조회
     */
    @Transactional(readOnly = true)
    fun getSessionDetail(sessionId: String): AdminSessionDto {
        val s = userSessionMapRepository.findBySessionId(sessionId)
            ?: throw NoSuchElementException("Session not found: $sessionId")

        return AdminSessionDto(
            id = requireNotNull(s.id) { "user_session_map.id is null" },
            sessionId = s.sessionId,
            userId = s.userId,
            deviceId = s.deviceId,
            provider = s.provider,
            createdAt = requireNotNull(s.createdAt) { "user_session_map.createdAt is null" },
            lastAccessedAt = s.lastAccessedAt,
            revokedAt = s.revokedAt,
            revokedReason = s.revokedReason,
        )
    }
}
