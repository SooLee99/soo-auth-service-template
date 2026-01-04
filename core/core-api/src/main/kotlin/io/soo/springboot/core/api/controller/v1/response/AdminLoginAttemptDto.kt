package io.soo.springboot.core.api.controller.v1.response

import io.soo.springboot.core.enums.AuthProvider
import java.time.LocalDateTime

data class AdminLoginAttemptDto(
    val id: Long,
    val success: Boolean,
    val provider: AuthProvider,
    val userId: Long?,
    val providerUserId: String?,
    val deviceId: String?,
    val ip: String?,
    val userAgent: String?,
    val errorCode: String?,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
)
