package io.soo.springboot.core.domain

import io.soo.springboot.core.enums.AuthProvider

import io.soo.springboot.storage.db.core.LocalCredentialEntity
import io.soo.springboot.storage.db.core.LocalCredentialRepository
import io.soo.springboot.storage.db.core.UserAccountEntity
import io.soo.springboot.storage.db.core.UserAccountRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class LocalAuthService(
    private val userAccountRepository: UserAccountRepository,
    private val localCredentialRepository: LocalCredentialRepository,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val securityContextRepository: SecurityContextRepository,
    private val sessionMapService: UserSessionMapService,
    private val userDeviceService: UserDeviceService,
    private val deviceBlockService: DeviceBlockService,
    private val loginAttemptService: LoginAttemptService,
) {

    @Transactional
    fun signUpAndLogin(
        email: String,
        rawPassword: String,
        nickname: String?,
        deviceId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResult {
        val normalizedEmail = email.trim().lowercase()

        validatePassword(rawPassword)

        if (userAccountRepository.findByEmail(normalizedEmail) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already exists")
        }

        val user = userAccountRepository.save(
            UserAccountEntity(
                email = normalizedEmail,
                nickname = nickname,
                profileImageUrl = null,
                thumbnailImageUrl = null,
            )
        )

        localCredentialRepository.save(
            LocalCredentialEntity(
                userId = user.id,
                passwordHash = passwordEncoder.encode(rawPassword),
                passwordUpdatedAt = LocalDateTime.now(),
            )
        )

        // ✅ 가입 직후 자동 로그인
        return login(normalizedEmail, rawPassword, deviceId, request, response)
    }

    fun login(
        email: String,
        rawPassword: String,
        deviceId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResult {
        val normalizedEmail = email.trim().lowercase()
        val ip = request.remoteAddr
        val ua = request.getHeader("User-Agent")

        try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(normalizedEmail, rawPassword)
            )

            val principal = auth.principal as UserPrincipal
            val userId = principal.userId

            // ✅ 디바이스 차단 체크
            if (deviceBlockService.isBlocked(userId, deviceId)) {
                loginAttemptService.record(
                    success = false,
                    provider = AuthProvider.LOCAL,
                    userId = userId,
                    providerUserId = normalizedEmail,
                    deviceId = deviceId,
                    ip = ip,
                    userAgent = ua,
                    errorCode = "DEVICE_BLOCKED",
                    errorMessage = "Blocked device",
                )
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Blocked device")
            }

            // ✅ 세션 생성 + SecurityContext 저장
            request.getSession(true)

            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = auth
            SecurityContextHolder.setContext(context)
            securityContextRepository.saveContext(context, request, response)

            val sessionId = request.getSession(false)!!.id
            val deviceIdForMap = deviceId?.takeIf { it.isNotBlank() } ?: UserDeviceService.UNKNOWN_DEVICE

            // ✅ 로그인 시도 기록(성공)
            loginAttemptService.record(
                success = true,
                provider = AuthProvider.LOCAL,
                userId = userId,
                providerUserId = normalizedEmail,
                deviceId = deviceIdForMap,
                ip = ip,
                userAgent = ua,
                errorCode = null,
                errorMessage = null,
            )

            // ✅ 디바이스 upsert + 세션 매핑 저장
            userDeviceService.upsertLogin(userId, deviceIdForMap, ip, ua)
            sessionMapService.bind(sessionId, userId, deviceIdForMap, AuthProvider.LOCAL)

            return LoginResult(
                sessionId = sessionId,
                userId = userId,
                provider = AuthProvider.LOCAL,
                deviceId = deviceIdForMap,
            )
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            // 인증 실패 기록
            loginAttemptService.record(
                success = false,
                provider = AuthProvider.LOCAL,
                userId = null,
                providerUserId = normalizedEmail,
                deviceId = deviceId,
                ip = ip,
                userAgent = ua,
                errorCode = "BAD_CREDENTIALS",
                errorMessage = e.message,
            )
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
    }

    private fun validatePassword(raw: String) {
        if (raw.length < 8) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 chars")
        // 숫자/대문자/특수문자 등
    }

    data class LoginResult(
        val sessionId: String,
        val userId: Long,
        val provider: AuthProvider,
        val deviceId: String,
    )
}
