package io.soo.springboot.core.domain

import io.soo.springboot.core.enums.AuthProvider
import io.soo.springboot.storage.db.core.OAuthIdentityEntity
import io.soo.springboot.storage.db.core.OAuthIdentityRepository
import io.soo.springboot.storage.db.core.UserAccountEntity
import io.soo.springboot.storage.db.core.UserAccountRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAccountService(
    private val userAccountRepository: UserAccountRepository,
    private val oauthIdentityRepository: OAuthIdentityRepository,
) {

    companion object {
        private const val MAX_PROVIDER_USER_ID = 255
    }

    @Transactional
    fun upsertFromOAuth2(token: OAuth2AuthenticationToken, request: HttpServletRequest): Pair<Long, String> {
        val info = extractUserInfo(token)
        val provider = info.provider
        val providerUserId = info.providerUserId.trim().take(MAX_PROVIDER_USER_ID)

        // 1) oauth_identity 먼저 찾기 (락 걸면 더 안전)
        val existingIdentity = oauthIdentityRepository.lockByProviderAndProviderUserId(provider, providerUserId)

        val user: UserAccountEntity = if (existingIdentity != null) {
            userAccountRepository.findById(existingIdentity.userId).orElseThrow {
                IllegalStateException("oauth_identity.userId=${existingIdentity.userId} not found")
            }
        } else {
            // 2) 없으면 이메일로 유저 찾거나 생성
            val byEmail = info.email?.let { userAccountRepository.findByEmail(it) }
            val createdOrExisting = byEmail ?: userAccountRepository.save(
                UserAccountEntity(
                    email = info.email,
                    nickname = info.nickname,
                    profileImageUrl = info.profileImageUrl,
                    thumbnailImageUrl = info.thumbnailImageUrl,
                )
            )

            // 3) oauth_identity 연결 (동시성으로 uq 충돌 가능 → 재시도 처리)
            val identity = try {
                oauthIdentityRepository.save(
                    OAuthIdentityEntity(
                        userId = createdOrExisting.id,
                        provider = provider,
                        providerUserId = providerUserId,
                        rawAttributesJson = request.toString()
                    )
                )
            } catch (_: DataIntegrityViolationException) {
                // 누군가 동시에 insert 한 경우: 재조회해서 그 userId를 따른다
                oauthIdentityRepository.lockByProviderAndProviderUserId(provider, providerUserId)
                    ?: throw IllegalStateException("oauth_identity upsert retry failed: $provider/$providerUserId")
            }

            // 만약 경쟁 상황에서 다른 userId로 매핑이 먼저 생겼다면 그 쪽을 최종 사용자로 선택
            if (identity.userId != createdOrExisting.id) {
                userAccountRepository.findById(identity.userId).orElseThrow {
                    IllegalStateException("oauth_identity.userId=${identity.userId} not found")
                }
            } else {
                createdOrExisting
            }
        }

        // 4) 최신 프로필 업데이트 (null이면 기존 값 유지)
        user.email = user.email ?: info.email
        user.nickname = info.nickname ?: user.nickname
        user.profileImageUrl = info.profileImageUrl ?: user.profileImageUrl
        user.thumbnailImageUrl = info.thumbnailImageUrl ?: user.thumbnailImageUrl
        userAccountRepository.save(user)

        return user.id to providerUserId
    }

    fun extractUserInfo(token: OAuth2AuthenticationToken): OAuthUserInfo {
        val regId = token.authorizedClientRegistrationId.lowercase()
        val attrs = token.principal.attributes

        return when (regId) {
            "kakao" -> {
                val id = (attrs["id"] ?: error("kakao id missing")).toString()
                val kakaoAccount = attrs["kakao_account"] as? Map<*, *>
                val profile = kakaoAccount?.get("profile") as? Map<*, *>

                OAuthUserInfo(
                    provider = AuthProvider.KAKAO,
                    providerUserId = id,
                    email = kakaoAccount?.get("email") as? String,
                    nickname = profile?.get("nickname") as? String,
                    thumbnailImageUrl = profile?.get("thumbnail_image_url") as? String,
                    profileImageUrl = profile?.get("profile_image_url") as? String,
                )
            }

            "naver" -> {
                val resp = attrs["response"] as? Map<*, *> ?: error("naver response missing")
                val id = (resp["id"] ?: error("naver id missing")).toString()

                OAuthUserInfo(
                    provider = AuthProvider.NAVER,
                    providerUserId = id,
                    email = resp["email"] as? String,
                    nickname = (resp["nickname"] as? String) ?: (resp["name"] as? String),
                    thumbnailImageUrl = resp["profile_image"] as? String,
                    profileImageUrl = resp["profile_image"] as? String,
                )
            }

            "google" -> {
                val id = (attrs["sub"] ?: error("google sub missing")).toString()
                OAuthUserInfo(
                    provider = AuthProvider.GOOGLE,
                    providerUserId = id,
                    email = attrs["email"] as? String,
                    nickname = (attrs["name"] as? String) ?: (attrs["given_name"] as? String),
                    thumbnailImageUrl = attrs["picture"] as? String,
                    profileImageUrl = attrs["picture"] as? String,
                )
            }
            else -> error("Unsupported provider: $regId")
        }
    }
}
