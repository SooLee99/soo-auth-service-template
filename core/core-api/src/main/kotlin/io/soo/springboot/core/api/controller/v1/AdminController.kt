package io.soo.springboot.core.api.controller.v1

import io.soo.springboot.core.api.controller.v1.response.AdminLoginAttemptDto
import io.soo.springboot.core.api.controller.v1.response.AdminUserDetailDto
import io.soo.springboot.core.api.controller.v1.response.AdminUserSummaryDto
import io.soo.springboot.core.domain.AdminQueryService
import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.core.support.response.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

data class RevokeSessionRequest(val reason: String? = null)
data class RevokeAllSessionsRequest(val deviceId: String? = null, val reason: String? = null)
data class BlockDeviceRequest(val reason: String)

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminQueryService: AdminQueryService,
) {

    // ✅ 유저 목록
    @GetMapping("/users")
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<List<AdminUserSummaryDto>> {
        val r = adminQueryService.listUsers(page, size)
        return ApiResponse.success(r.items, r.page)
    }

    // ✅ 유저 상세 (연동/디바이스/세션/최근로그인시도 포함)
    @GetMapping("/users/{userId}")
    fun getUserDetail(@PathVariable userId: Long): ApiResponse<AdminUserDetailDto> =
        ApiResponse.success(adminQueryService.getUserDetail(userId))

    // ✅ 로그인 시도 목록 조회
    @GetMapping("/login-attempts")
    fun searchLoginAttempts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(required = false) success: Boolean?,
        @RequestParam(required = false) provider: AuthProvider?,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) deviceId: String?,
        @RequestParam(required = false) ip: String?,
        @RequestParam(required = false) errorCode: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
    ): ApiResponse<List<AdminLoginAttemptDto>> {
        val r = adminQueryService.searchLoginAttempts(
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

    // ✅ 특정 세션 강제 종료(DB revoke)
    @PostMapping("/sessions/{sessionId}/revoke")
    fun revokeSession(
        @PathVariable sessionId: String,
        @RequestBody req: RevokeSessionRequest,
    ): ApiResponse<Unit> {
        adminQueryService.revokeSession(sessionId, req.reason)
        return ApiResponse.success()
    }

    // ✅ 유저 세션 전체(또는 특정 device) 강제 종료
    @PostMapping("/users/{userId}/sessions/revoke")
    fun revokeAllSessions(
        @PathVariable userId: Long,
        @RequestBody req: RevokeAllSessionsRequest,
    ): ApiResponse<Unit> {
        adminQueryService.revokeAllSessions(userId, req.deviceId, req.reason)
        return ApiResponse.success(Unit)
    }

    // ✅ 디바이스 차단
    @PostMapping("/users/{userId}/devices/{deviceId}/block")
    fun blockDevice(
        @PathVariable userId: Long,
        @PathVariable deviceId: String,
        @RequestBody req: BlockDeviceRequest,
    ): ApiResponse<Unit> {
        adminQueryService.blockDevice(userId, deviceId, req.reason)
        return ApiResponse.success(Unit)
    }

    // ✅ 디바이스 차단 해제
    @PostMapping("/users/{userId}/devices/{deviceId}/unblock")
    fun unblockDevice(
        @PathVariable userId: Long,
        @PathVariable deviceId: String,
    ): ApiResponse<Unit> {
        adminQueryService.unblockDevice(userId, deviceId)
        return ApiResponse.success(Unit)
    }
}
