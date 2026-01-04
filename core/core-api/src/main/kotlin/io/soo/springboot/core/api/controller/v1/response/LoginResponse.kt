package io.soo.springboot.core.api.controller.v1.response

import io.soo.springboot.core.domain.LocalAuthService

data class LoginResponse(
    val sessionId: String,
    val userId: Long,
    val provider: String,
    val deviceId: String,
) {
    companion object {
        fun from(r: LocalAuthService.LoginResult) = LoginResponse(
            sessionId = r.sessionId,
            userId = r.userId,
            provider = r.provider.name,
            deviceId = r.deviceId,
        )
    }
}
