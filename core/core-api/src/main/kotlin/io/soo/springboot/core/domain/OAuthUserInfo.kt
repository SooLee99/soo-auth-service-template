package io.soo.springboot.core.domain

import io.soo.springboot.core.enums.AuthProvider

data class OAuthUserInfo(
    val provider: AuthProvider,
    val providerUserId: String,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val thumbnailImageUrl: String?,
)
