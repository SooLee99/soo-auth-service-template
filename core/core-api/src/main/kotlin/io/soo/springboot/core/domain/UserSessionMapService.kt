package io.soo.springboot.core.domain

import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.storage.db.core.UserSessionMapEntity
import io.soo.springboot.storage.db.core.UserSessionMapRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserSessionMapService(
    private val repo: UserSessionMapRepository,
) {
    companion object {
        private const val MAX_SESSION_ID = 100
        private const val MAX_DEVICE_ID = 255
    }

    private fun normSessionId(sessionId: String) = sessionId.trim().take(MAX_SESSION_ID)
    private fun normDeviceId(deviceId: String) = deviceId.trim().take(MAX_DEVICE_ID)

    /**
     * session_id 유니크를 전제로 "upsert bind"로 동작 (중복 저장/경쟁 조건 방지)
     */
    @Transactional
    fun bind(sessionId: String, userId: Long, deviceId: String, provider: AuthProvider) {
        val now = LocalDateTime.now()
        val sid = normSessionId(sessionId)
        val did = normDeviceId(deviceId)

        val entity = repo.lockBySessionId(sid)
            ?: UserSessionMapEntity(
                sessionId = sid,
                userId = userId,
                deviceId = did,
                provider = provider,
                lastAccessedAt = now,
                revokedAt = null,
                revokedReason = null,
            )

        // 이미 있던 세션이면 바인딩 정보 갱신 (재로그인/재발급 대응)
        entity.userId = userId
        entity.deviceId = did
        entity.provider = provider
        entity.lastAccessedAt = now
        entity.revokedAt = null
        entity.revokedReason = null

        repo.save(entity)
    }

    fun findActive(sessionId: String): UserSessionMapEntity? =
        repo.findBySessionIdAndRevokedAtIsNull(normSessionId(sessionId))

    @Transactional
    fun touch(sessionId: String) {
        val sid = normSessionId(sessionId)
        val m = repo.lockBySessionId(sid) ?: return
        if (m.revokedAt != null) return

        m.lastAccessedAt = LocalDateTime.now()
        repo.save(m)
    }

    fun activeSessionIds(userId: Long, deviceId: String? = null): List<String> =
        if (deviceId.isNullOrBlank())
            repo.findActiveSessionIdsByUserId(userId)
        else
            repo.findActiveSessionIdsByUserIdAndDeviceId(userId, normDeviceId(deviceId))

    @Transactional
    fun revokeSession(sessionId: String, reason: String?) {
        val sid = normSessionId(sessionId)
        val m = repo.lockBySessionId(sid) ?: return
        if (m.revokedAt != null) return

        m.revokedAt = LocalDateTime.now()
        m.revokedReason = reason
        repo.save(m)
    }

    @Transactional
    fun revokeAll(userId: Long, deviceId: String? = null, reason: String?) {
        val list = if (deviceId.isNullOrBlank())
            repo.findAllByUserIdAndRevokedAtIsNull(userId)
        else
            repo.findAllByUserIdAndDeviceIdAndRevokedAtIsNull(userId, normDeviceId(deviceId))

        val now = LocalDateTime.now()
        list.forEach {
            it.revokedAt = now
            it.revokedReason = reason
        }
        repo.saveAll(list)
    }

    fun getBinding(sessionId: String): UserSessionMapEntity? = findActive(sessionId)
}
