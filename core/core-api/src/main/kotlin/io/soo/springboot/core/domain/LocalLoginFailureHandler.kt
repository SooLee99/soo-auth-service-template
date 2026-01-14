package io.soo.springboot.core.domain

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

import io.soo.springboot.core.enums.AuthProvider

@Component
class LocalLoginFailureHandler(
    private val localLoginPolicyService: LocalLoginPolicyService,
    private val loginAttemptService: LoginAttemptService,
    private val objectMapper: ObjectMapper,
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val ip = request.remoteAddr
        val ua = request.getHeader("User-Agent")
        val deviceId = request.getHeader(LocalJsonLoginFilter.HEADER_DEVICE_ID)?.takeIf { it.isNotBlank() }

        val normalizedEmail = request.getAttribute(LocalJsonLoginFilter.ATTR_NORMALIZED_EMAIL) as? String
            ?: "unknown"

        // ✅ 실패 카운트/잠금 정책 적용(잠금 중이면 lockUntil=now+1분 갱신 포함)
        val result = localLoginPolicyService.recordFailureByEmail(
            normalizedEmail = normalizedEmail,
            now = LocalDateTime.now()
        )

        val (status, errorCode, message) = when (result) {
            LocalLoginPolicyService.FailureResult.LOCKED ->
                Triple(429, "LOCKED", "로그인 시도 횟수가 초과되었습니다. 잠시 후 다시 시도해 주세요.")
            LocalLoginPolicyService.FailureResult.BAD_CREDENTIALS ->
                Triple(401, "BAD_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.")
            LocalLoginPolicyService.FailureResult.NOT_FOUND ->
                Triple(401, "BAD_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        // ✅ 로그인 시도 기록(실패)
        loginAttemptService.record(
            success = false,
            provider = AuthProvider.LOCAL,
            userId = null, // email만으로는 userId를 노출하지 않기 위해 null로 기록(정책에 따라 변경 가능)
            providerUserId = normalizedEmail,
            deviceId = deviceId,
            ip = ip,
            userAgent = ua,
            errorCode = errorCode,
            errorMessage = exception.message,
        )

        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.outputStream, mapOf("code" to errorCode, "message" to message))
    }
}
