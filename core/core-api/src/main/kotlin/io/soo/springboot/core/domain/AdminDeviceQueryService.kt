package io.soo.springboot.core.domain

import io.soo.springboot.core.api.controller.v1.response.AdminUserDeviceDto
import io.soo.springboot.storage.db.core.UserDeviceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminDeviceQueryService(
    private val userDeviceRepository: UserDeviceRepository,
) {

    /**
     * ✅ 특정 유저의 디바이스 목록 조회
     * - 운영 콘솔에서 “디바이스 목록 먼저 보고 조치”가 일반적이므로 별도 API 권장
     */
    @Transactional(readOnly = true)
    fun listUserDevices(userId: Long): List<AdminUserDeviceDto> {
        return userDeviceRepository.findAllByUserId(userId).map {
            AdminUserDeviceDto(
                id = it.id,
                userId = it.userId,
                deviceId = it.deviceId,
                status = it.deviceStatus,
                blockedReason = it.blockedReason,
                blockedAt = it.blockedAt,
                lastLoginAt = it.lastLoginAt,
                lastIp = it.lastIp,
                lastUserAgent = it.lastUserAgent,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }
}
