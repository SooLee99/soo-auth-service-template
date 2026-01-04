package io.soo.springboot.core.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.context.HttpSessionSecurityContextRepository

@Configuration
class SecurityContextConfig {

    @Bean
    fun securityContextRepository(): SecurityContextRepository =
        HttpSessionSecurityContextRepository()
}
