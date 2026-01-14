package io.soo.springboot.core.domain

import io.soo.springboot.storage.db.core.JwtDenylistEntity
import io.soo.springboot.storage.db.core.JwtDenylistRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

@Service
class JwtDenylistService(
    private val repo: JwtDenylistRepository,
) {

    /**
     * ✅ JWT revoke(블랙리스트 등록)
     * - jti가 없을 수도 있어서 tokenHash 기반도 함께 저장/검증
     */
    @Transactional
    fun revoke(
        tokenValue: String,
        jti: String?,
        expiresAt: Instant?,
        reason: String? = null,
    ) {
        val exp = expiresAt ?: Instant.now().plusSeconds(60 * 60) // 정책에 맞게 조정
        val tokenHash = sha256(tokenValue)

        // ✅ jti가 있다면 jti 기준 멱등 처리
        if (!jti.isNullOrBlank() && repo.existsByJti(jti)) return

        // ✅ jti가 없거나, 혹은 동일 토큰이 이미 등록된 경우 멱등 처리
        if (repo.existsByTokenHash(tokenHash)) return

        // jti가 없다면 내부 키로 hash 기반 jti를 만들어 저장(조회 편의)
        val key = jti?.takeIf { it.isNotBlank() } ?: "HASH:$tokenHash"

        repo.save(
            JwtDenylistEntity(
                jti = key,
                revokedAt = Instant.now(),
                expiresAt = exp,
                reason = reason,
            )
        )
    }

    /**
     * ✅ (Decoder용) jti 또는 tokenHash로 revoke 여부 확인
     */
    @Transactional(readOnly = true)
    fun isRevoked(jti: String?, tokenValue: String): Boolean {
        if (!jti.isNullOrBlank() && repo.existsByJti(jti)) return true

        val tokenHash = sha256(tokenValue)
        return repo.existsByTokenHash(tokenHash) || repo.existsByJti("HASH:$tokenHash")
    }

    /**
     * ✅ 만료된 denylist 레코드 정리
     */
    @Transactional
    fun cleanupExpired(now: Instant = Instant.now()): Int {
        return repo.deleteExpired(now)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
