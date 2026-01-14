package io.soo.springboot.core.domain

import org.springframework.stereotype.Component

@Component
class OAuth2UserAttributeParser {

    fun extractProviderUserId(provider: String, attrs: Map<String, Any?>): String? =
        when (provider) {
            "kakao" -> (attrs["id"] as? Number)?.toLong()?.toString()
            "naver" -> (attrs["response"] as? Map<*, *>)?.get("id")?.toString()
            "google" -> attrs["sub"]?.toString() ?: attrs["id"]?.toString()
            else -> attrs["id"]?.toString()
        }

    fun extractEmail(provider: String, attrs: Map<String, Any?>): String? =
        when (provider) {
            "kakao" -> {
                val kakaoAccount = attrs["kakao_account"] as? Map<*, *>
                kakaoAccount?.get("email")?.toString()
            }
            "naver" -> (attrs["response"] as? Map<*, *>)?.get("email")?.toString()
            "google" -> attrs["email"]?.toString()
            else -> attrs["email"]?.toString()
        }

    fun extractNickname(provider: String, attrs: Map<String, Any?>): String? =
        when (provider) {
            "kakao" -> {
                val kakaoAccount = attrs["kakao_account"] as? Map<*, *>
                val profile = kakaoAccount?.get("profile") as? Map<*, *>
                profile?.get("nickname")?.toString()
            }
            "naver" -> (attrs["response"] as? Map<*, *>)?.get("nickname")?.toString()
            "google" -> attrs["name"]?.toString()
            else -> attrs["name"]?.toString()
        }

    fun extractProfileImageUrl(provider: String, attrs: Map<String, Any?>): String? =
        when (provider) {
            "kakao" -> {
                val kakaoAccount = attrs["kakao_account"] as? Map<*, *>
                val profile = kakaoAccount?.get("profile") as? Map<*, *>
                (profile?.get("profile_image_url") ?: profile?.get("thumbnail_image_url"))?.toString()
            }
            "naver" -> (attrs["response"] as? Map<*, *>)?.get("profile_image")?.toString()
            "google" -> attrs["picture"]?.toString()
            else -> attrs["picture"]?.toString()
        }
}
