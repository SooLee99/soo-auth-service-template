package io.soo.springboot.core.api.controller.v1.response

data class TokenInfoResponseDto(
    val tokenType: String,
    val expiresAt: String?,
    val scopes: List<String>,
)
