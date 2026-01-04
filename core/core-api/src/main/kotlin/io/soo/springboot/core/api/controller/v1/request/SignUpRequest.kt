package io.soo.springboot.core.api.controller.v1.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String? = null,
)
