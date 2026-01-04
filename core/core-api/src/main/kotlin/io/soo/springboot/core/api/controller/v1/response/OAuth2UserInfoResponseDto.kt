package io.soo.springboot.core.api.controller.v1.response

data class OAuth2UserInfoResponseDto(
    val providerUserId: String?,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?,
)
