package io.soo.springboot.core.api.config

import io.soo.springboot.core.domain.DevicePolicyFilter
import io.soo.springboot.core.domain.OAuth2LoginFailureHandler
import io.soo.springboot.core.domain.OAuth2LoginSuccessHandler
import io.soo.springboot.core.domain.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
class SecurityConfig(
    private val successHandler: OAuth2LoginSuccessHandler,
    private val failureHandler: OAuth2LoginFailureHandler,
    private val devicePolicyFilter: DevicePolicyFilter,
    private val userDetailsService: UserDetailsServiceImpl,
    private val securityContextRepository: SecurityContextRepository,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(cfg: AuthenticationConfiguration): AuthenticationManager =
        cfg.authenticationManager

    @Bean
    fun daoAuthProvider(passwordEncoder: PasswordEncoder): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityContext { it.securityContextRepository(securityContextRepository) }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers(
                    "/api/v1/auth/**",
                    "/oauth2/**",
                    "/login/**",
                    "/h2-console/**",
                )
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/auth/**",
                    "/oauth2/**",
                    "/login/**",
                    "/error",
                    "/actuator/**",
                ).permitAll()
                it.requestMatchers("/api/v1/admin/**").permitAll()//.hasRole("ADMIN")
                it.requestMatchers("/h2-console/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .oauth2Login { oauth ->
                oauth.successHandler(successHandler)
                oauth.failureHandler(failureHandler)
            }
            // 로그인 이후 요청마다 차단/세션revoke 체크
            .addFilterAfter(devicePolicyFilter, AuthorizationFilter::class.java)
        return http.build()
    }
}
