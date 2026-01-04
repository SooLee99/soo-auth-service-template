package io.soo.springboot.core.domain


import io.soo.springboot.core.api.controller.v1.response.AdminLoginAttemptDto
import io.soo.springboot.core.api.controller.v1.response.AdminOAuthIdentityDto
import io.soo.springboot.core.api.controller.v1.response.AdminSessionDto
import io.soo.springboot.core.api.controller.v1.response.AdminUserDetailDto
import io.soo.springboot.core.api.controller.v1.response.AdminUserDeviceDto
import io.soo.springboot.core.api.controller.v1.response.AdminUserSummaryDto
import io.soo.springboot.core.api.controller.v1.response.PagedResult
import io.soo.springboot.core.api.controller.v1.response.toPageMetaDto
import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.core.enums.DeviceStatus
import io.soo.springboot.storage.db.core.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminQueryService(
    private val userAccountRepository: UserAccountRepository,
    private val oauthIdentityRepository: OAuthIdentityRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val userSessionMapRepository: UserSessionMapRepository,
    private val loginAttemptRepository: LoginAttemptRepository,
) {

    @Transactional(readOnly = true)
    fun listUsers(page: Int, size: Int): PagedResult<AdminUserSummaryDto> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val p = userAccountRepository.findAll(pageable)

        val items = p.content.map { u ->
            val identities = oauthIdentityRepository.findAllByUserId(u.id)
            val providers = identities.map { it.provider }.distinct()

            AdminUserSummaryDto(
                userId = u.id,
                email = u.email,
                nickname = u.nickname,
                profileImageUrl = u.profileImageUrl,
                createdAt = u.createdAt,
                updatedAt = u.updatedAt,
                providers = providers,
                deviceCount = userDeviceRepository.findAllByUserId(u.id).size,
                activeSessionCount = userSessionMapRepository.countActiveByUserId(u.id),
            )
        }

        return PagedResult(items = items, page = p.toPageMetaDto())
    }


    @Transactional(readOnly = true)
    fun getUserDetail(userId: Long): AdminUserDetailDto {
        val user = userAccountRepository.findById(userId).orElseThrow { NoSuchElementException("User not found: $userId") }

        val identities = oauthIdentityRepository.findAllByUserId(userId).map {
            AdminOAuthIdentityDto(
                id = it.id,
                provider = it.provider,
                providerUserId = it.providerUserId,
                createdAt = it.createdAt,
            )
        }

        val devices = userDeviceRepository.findAllByUserId(userId).map {
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

        val sessions = userSessionMapRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map {
            AdminSessionDto(
                id = it.id,
                sessionId = it.sessionId,
                userId = it.userId,
                deviceId = it.deviceId,
                provider = it.provider,
                createdAt = it.createdAt,
                lastAccessedAt = it.lastAccessedAt,
                revokedAt = it.revokedAt,
                revokedReason = it.revokedReason,
            )
        }

        val attempts = loginAttemptRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).map {
            AdminLoginAttemptDto(
                id = it.id,
                success = it.success,
                provider = it.provider,
                userId = it.userId,
                providerUserId = it.providerUserId,
                deviceId = it.deviceId,
                ip = it.ip,
                userAgent = it.userAgent,
                errorCode = it.errorCode,
                errorMessage = it.errorMessage,
                createdAt = it.createdAt,
            )
        }

        return AdminUserDetailDto(
            userId = user.id,
            email = user.email,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            thumbnailImageUrl = user.thumbnailImageUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            identities = identities,
            devices = devices,
            sessions = sessions,
            lastLoginAttempts = attempts,
        )
    }

    @Transactional(readOnly = true)
    fun searchLoginAttempts(
        page: Int,
        size: Int,
        success: Boolean?,
        provider: AuthProvider?,
        userId: Long?,
        deviceId: String?,
        ip: String?,
        errorCode: String?,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): PagedResult<AdminLoginAttemptDto> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val spec = LoginAttemptSpecs.filter(success, provider, userId, deviceId, ip, errorCode, from, to)

        val p = loginAttemptRepository.findAll(spec, pageable)
        val items = p.content.map {
            AdminLoginAttemptDto(
                id = it.id,
                success = it.success,
                provider = it.provider,
                userId = it.userId,
                providerUserId = it.providerUserId,
                deviceId = it.deviceId,
                ip = it.ip,
                userAgent = it.userAgent,
                errorCode = it.errorCode,
                errorMessage = it.errorMessage,
                createdAt = it.createdAt,
            )
        }

        return PagedResult(items = items, page = p.toPageMetaDto())
    }


    @Transactional
    fun revokeSession(sessionId: String, reason: String?) {
        val m = userSessionMapRepository.findBySessionId(sessionId)
            ?: throw NoSuchElementException("Session not found: $sessionId")

        if (m.revokedAt != null) return
        m.revokedAt = LocalDateTime.now()
        m.revokedReason = reason
        userSessionMapRepository.save(m)
    }

    @Transactional
    fun revokeAllSessions(userId: Long, deviceId: String?, reason: String?) {
        val list = if (deviceId.isNullOrBlank()) {
            userSessionMapRepository.findAllByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId)
        } else {
            userSessionMapRepository.findAllByUserIdAndDeviceIdAndRevokedAtIsNull(userId, deviceId)
        }

        val now = LocalDateTime.now()
        list.forEach {
            it.revokedAt = now
            it.revokedReason = reason
        }
        userSessionMapRepository.saveAll(list)
    }

    @Transactional
    fun blockDevice(userId: Long, deviceId: String, reason: String) {
        val d = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
            ?: throw NoSuchElementException("Device not found. userId=$userId deviceId=$deviceId")

        d.deviceStatus = DeviceStatus.BLOCKED
        d.blockedReason = reason
        d.blockedAt = LocalDateTime.now()
        userDeviceRepository.save(d)
    }

    @Transactional
    fun unblockDevice(userId: Long, deviceId: String) {
        val d = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
            ?: throw NoSuchElementException("Device not found. userId=$userId deviceId=$deviceId")

        d.deviceStatus = DeviceStatus.ACTIVE
        d.blockedReason = null
        d.blockedAt = null
        userDeviceRepository.save(d)
    }
}
