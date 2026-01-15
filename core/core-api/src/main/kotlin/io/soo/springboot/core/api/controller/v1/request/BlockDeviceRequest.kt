package io.soo.springboot.core.api.controller.v1.request

data class BlockDeviceRequest(val reason: String)
data class RevokeDeviceSessionsRequest(
    val reason: String? = null,
)
