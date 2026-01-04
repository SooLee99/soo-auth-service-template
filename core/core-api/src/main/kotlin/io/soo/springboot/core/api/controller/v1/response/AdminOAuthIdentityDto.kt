package io.soo.springboot.core.api.controller.v1.response

import io.soo.springboot.core.enums.AuthProvider
import java.time.LocalDateTime

data class AdminOAuthIdentityDto(
    val id: Long,
    val provider: AuthProvider,
    val providerUserId: String,
    val createdAt: LocalDateTime,
)
