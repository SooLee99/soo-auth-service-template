package io.soo.springboot.core.api.controller.v1.request


data class RevokeSessionRequest(val reason: String? = null)
data class RevokeAllSessionsRequest(val deviceId: String? = null, val reason: String? = null)