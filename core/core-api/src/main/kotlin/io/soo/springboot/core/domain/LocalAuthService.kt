package io.soo.springboot.core.domain

import io.soo.springboot.core.api.controller.v1.response.CredentialStatus
import io.soo.springboot.core.api.controller.v1.response.SignUpProfile
import io.soo.springboot.core.api.controller.v1.response.SignUpResult
import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.storage.db.core.LocalCredentialEntity
import io.soo.springboot.storage.db.core.LocalCredentialRepository
import io.soo.springboot.storage.db.core.UserAccountEntity
import io.soo.springboot.storage.db.core.UserAccountRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class LocalAuthService(
    private val userAccountRepository: UserAccountRepository,
    private val localCredentialRepository: LocalCredentialRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    /**
     * ✅ 로컬 회원가입만 수행합니다.
     */
    @Transactional
    fun signUp(
        email: String,
        rawPassword: String,
        name: String?,
        nickname: String?,
        profileImageUrl: String?,
        thumbnailImageUrl: String?,
        birthDate: LocalDate?,
    ): SignUpResult {
        // 1) 이메일 중복 검사 (LOCAL 가입은 이메일이 고유 식별자 역할)
        if (userAccountRepository.findByEmail(email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일이 존재합니다.")
        }

        // 2) 계정 생성
        val user = userAccountRepository.save(
            UserAccountEntity(
                email = email,
                emailVerified = false,
                name = name,
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                thumbnailImageUrl = thumbnailImageUrl,
                birthdate = birthDate,
            ),
        )

        // 3) 로컬 자격 증명 저장
        val credential = localCredentialRepository.save(
            LocalCredentialEntity(
                userId = user.id,
                passwordHash = passwordEncoder.encode(rawPassword),
                passwordUpdatedAt = LocalDateTime.now(),
            ),
        )

        // 4) 반환값: 가입 완료 후 화면/클라이언트에서 바로 쓸 수 있도록 "공용 정보" 중심으로 구성
        return SignUpResult(
            userId = user.id,
            email = email,
            emailVerified = user.emailVerified,
            provider = AuthProvider.LOCAL,
            profile = SignUpProfile(
                name = user.name,
                nickname = user.nickname,
                profileImageUrl = user.profileImageUrl,
                thumbnailImageUrl = user.thumbnailImageUrl,
                birthDate = user.birthdate,
            ),
            credentialStatus = CredentialStatus(
                passwordUpdatedAt = credential.passwordUpdatedAt,
                failedLoginCount = credential.failedLoginCount,
                lockUntil = credential.lockUntil,
            ),
            createdAt = user.createdAt,
        )
    }
}
