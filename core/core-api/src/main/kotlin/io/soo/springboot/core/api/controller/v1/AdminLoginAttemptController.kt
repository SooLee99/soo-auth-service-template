package io.soo.springboot.core.api.controller.v1

import io.soo.springboot.core.api.controller.v1.response.AdminLoginAttemptDto
import io.soo.springboot.core.api.controller.v1.response.AdminLoginAttemptStatsDto
import io.soo.springboot.core.domain.AdminLoginAttemptQueryService
import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.core.support.response.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/admin/login-attempts")
class AdminLoginAttemptController(
    private val loginAttemptQuery: AdminLoginAttemptQueryService,
) {

    /**
     * ✅ 로그인 시도 목록 조회
     * GET /api/v1/admin/login-attempts?...filters...
     */
    @GetMapping
    fun searchLoginAttempts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(required = false) success: Boolean?,
        @RequestParam(required = false) provider: AuthProvider?,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) deviceId: String?,
        @RequestParam(required = false) ip: String?,
        @RequestParam(required = false) errorCode: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: LocalDateTime?,
    ): ApiResponse<List<AdminLoginAttemptDto>> {
        val r = loginAttemptQuery.searchLoginAttempts(
            page = page,
            size = size,
            success = success,
            provider = provider,
            userId = userId,
            deviceId = deviceId,
            ip = ip,
            errorCode = errorCode,
            from = from,
            to = to,
        )
        return ApiResponse.success(r.items, r.page)
    }

    /**
     * ✅ 로그인 시도 단건 상세 조회
     * GET /api/v1/admin/login-attempts/{attemptId}
     */
    @GetMapping("/{attemptId}")
    fun getLoginAttempt(
        @PathVariable attemptId: Long,
    ): ApiResponse<AdminLoginAttemptDto> {
        return ApiResponse.success(loginAttemptQuery.getLoginAttempt(attemptId))
    }

    /**
     * ✅ 로그인 시도 통계
     * GET /api/v1/admin/login-attempts/stats?from=...&to=...&topLimit=20
     *
     * - from/to를 안 주면 최근 7일 기준으로 집계(서비스에서 기본 처리)
     * - provider/userId/deviceId/ip로 범위를 좁혀 집계 가능
     */
    @GetMapping("/stats")
    fun getStats(
        @RequestParam(required = false) provider: AuthProvider?,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) deviceId: String?,
        @RequestParam(required = false) ip: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: LocalDateTime?,
        @RequestParam(defaultValue = "20") topLimit: Int,
    ): ApiResponse<AdminLoginAttemptStatsDto> {
        val stats = loginAttemptQuery.getStats(
            provider = provider,
            userId = userId,
            deviceId = deviceId,
            ip = ip,
            from = from,
            to = to,
            topLimit = topLimit.coerceIn(1, 200), // 과도한 조회 방지
        )
        return ApiResponse.success(stats)
    }

    /**
     * ✅ 특정 유저의 최근 로그인 시도 조회
     * GET /api/v1/admin/users/{userId}/login-attempts/recent?size=50
     */
    @GetMapping("/recent")
    fun recent(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "50") size: Int,
    ): ApiResponse<List<AdminLoginAttemptDto>> {
        val items = loginAttemptQuery.recentByUser(userId, size)
        return ApiResponse.success(items)
    }
}
