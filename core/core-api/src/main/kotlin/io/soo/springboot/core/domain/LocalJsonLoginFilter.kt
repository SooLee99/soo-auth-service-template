package io.soo.springboot.core.domain

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class LocalJsonLoginFilter(
    private val objectMapper: ObjectMapper,
) : UsernamePasswordAuthenticationFilter() {

    companion object {
        // ✅ 실패 핸들러에서 email을 알 수 있도록 request attribute에 저장
        const val ATTR_NORMALIZED_EMAIL = "ATTR_NORMALIZED_EMAIL"
        const val HEADER_DEVICE_ID = "X-Device-Id"
    }

    data class LoginRequest(
        val email: String,
        val password: String,
    )

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val req = objectMapper.readValue(request.inputStream, LoginRequest::class.java)

        val normalizedEmail = req.email.trim().lowercase()
        request.setAttribute(ATTR_NORMALIZED_EMAIL, normalizedEmail)

        val authRequest = UsernamePasswordAuthenticationToken(normalizedEmail, req.password)
        setDetails(request, authRequest)
        return this.authenticationManager.authenticate(authRequest)
    }
}
