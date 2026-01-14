package io.soo.springboot.core.api.controller.batch

import io.soo.springboot.core.domain.JwtDenylistService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DenylistCleanupScheduler(
    private val denylist: JwtDenylistService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * ✅ 만료된 JWT denylist 레코드 정리
     * - cron: 매시간 0분 0초
     * - 운영 환경에 따라 10분/30분 단위로 돌려도 괜찮습니다.
     */
    @Scheduled(cron = "0 0 * * * *")
    fun cleanup() {
        try {
            val deleted = denylist.cleanupExpired(Instant.now())
            if (deleted > 0) {
                log.info("[DenylistCleanupScheduler] expired rows deleted: {}", deleted)
            } else {
                log.debug("[DenylistCleanupScheduler] nothing to delete")
            }
        } catch (e: Exception) {
            log.error("[DenylistCleanupScheduler] cleanup failed", e)
        }
    }
}
