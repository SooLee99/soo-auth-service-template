package io.soo.springboot.core.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.soo.springboot.core.domain.*
import io.soo.springboot.core.DBSessionRevokeLogoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.AndRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

@Configuration
class SecurityConfig(
    // ✅ OAuth2 로그인 핸들러
    private val oauth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oauth2LoginFailureHandler: OAuth2LoginFailureHandler,

    // ✅ 로컬 로그인 핸들러
    private val localLoginSuccessHandler: LocalLoginSuccessHandler,
    private val localLoginFailureHandler: LocalLoginFailureHandler,

    // ✅ 로그인 이후 정책 필터
    private val devicePolicyFilter: DevicePolicyFilter,

    // ✅ DaoAuthenticationProvider 구성
    private val userDetailsService: UserDetailsServiceImpl,
    private val dbLogoutHandler: DBSessionRevokeLogoutHandler,
    private val securityContextRepository: SecurityContextRepository,
    private val objectMapper: ObjectMapper,
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
    fun jwtDecoder(delegate: JwtDecoder, denylist: JwtDenylistService): JwtDecoder {
        return RevocationAwareJwtDecoder(delegate, denylist)
    }

    // ✅ 로컬(JSON) 로그인 필터 빈 등록
    @Bean
    fun localJsonLoginFilter(authenticationManager: AuthenticationManager): LocalJsonLoginFilter {
        return LocalJsonLoginFilter(objectMapper).apply {
            setAuthenticationManager(authenticationManager)

            // ✅ /api/v1/auth/login 으로 로그인 처리
            setFilterProcessesUrl("/api/v1/auth/login")

            // ✅ 성공/실패 핸들러 연결
            setAuthenticationSuccessHandler(localLoginSuccessHandler)
            setAuthenticationFailureHandler(localLoginFailureHandler)
        }
    }

    // ✅ (1) JWT 기반 API 체인
    // - /api/v1/auth/** (회원가입/로그인/로그아웃 등)은 제외해야 세션 체인으로 처리 가능
    @Bean
    @Order(1)
    fun apiJwtChain(http: HttpSecurity): SecurityFilterChain {
        val apiMatcher = AndRequestMatcher(
            AntPathRequestMatcher("/api/**"),
            NegatedRequestMatcher(AntPathRequestMatcher("/api/v1/auth/**")),
        )

        http
            .securityMatcher(apiMatcher)
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2ResourceServer { it.jwt { } } // JwtDecoder 빈을 위에서 오버라이드
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }

        return http.build()
    }

    /**
     * ✅ (2) 세션/OAuth2/로컬로그인 체인
     * - OAuth2 로그인, 로컬(JSON) 로그인, 로그아웃, H2-console 등
     * - /api/v1/auth/login 은 LocalJsonLoginFilter가 처리 -> 컨트롤러 login API 불필요
     */
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        localJsonLoginFilter: LocalJsonLoginFilter,
    ): SecurityFilterChain {
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
                it.requestMatchers("/api/v1/admin/**").permitAll() // 필요시 hasRole("ADMIN")로 변경
                it.requestMatchers("/h2-console/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }

            // ✅ 폼로그인 비활성화(JSON 로그인 필터 사용)
            .formLogin { it.disable() }

            // ✅ OAuth2 로그인
            .oauth2Login { oauth ->
                oauth.successHandler(oauth2LoginSuccessHandler)
                oauth.failureHandler(oauth2LoginFailureHandler)
            }

            // ✅ 로컬(JSON) 로그인 필터를 UsernamePasswordAuthenticationFilter 위치에 등록
            .addFilterAt(localJsonLoginFilter, UsernamePasswordAuthenticationFilter::class.java)

            // ✅ 로그인 이후 요청마다 디바이스 차단/세션 revoke 체크
            .addFilterAfter(devicePolicyFilter, AuthorizationFilter::class.java)

            // ✅ 로그아웃
            .logout {
                it.logoutUrl("/api/v1/auth/logout")
                it.addLogoutHandler(dbLogoutHandler)
                it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler())
            }

        return http.build()
    }
}
