package io.soo.springboot.core.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
class SecurityContextRepoConfig {

    @Bean
    fun securityContextRepository(): SecurityContextRepository {
        // ✅ 세션(HttpSession)에 SecurityContext를 저장/복원하는 기본 구현체
        return HttpSessionSecurityContextRepository()
    }
}
