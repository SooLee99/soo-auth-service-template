package io.soo.springboot.core.api.controller.v1.response

import java.time.LocalDateTime

data class AdminUserDetailDto(
    val userId: Long?,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val thumbnailImageUrl: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val identities: List<AdminOAuthIdentityDto>,
    val devices: List<AdminUserDeviceDto>,
    val sessions: List<AdminSessionDto>,
    val lastLoginAttempts: List<AdminLoginAttemptDto>,
)
