package io.soo.springboot.core.api.controller.v1.response

import java.time.LocalDateTime

data class LoginSuccessResponseDto(
    val sessionId: String,
    val userId: Long,
    val deviceId: String,
    val provider: String,
    val createdAt: LocalDateTime,
    val lastAccessedAt: LocalDateTime?,
    val oauth2: OAuth2UserInfoResponseDto,
    val token: TokenInfoResponseDto?,
)
