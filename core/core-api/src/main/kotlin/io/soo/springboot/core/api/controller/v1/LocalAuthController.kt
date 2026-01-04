package io.soo.springboot.core.api.controller.v1

import io.soo.springboot.core.api.controller.v1.request.LoginRequest
import io.soo.springboot.core.api.controller.v1.response.LoginResponse
import io.soo.springboot.core.api.controller.v1.request.SignUpRequest
import io.soo.springboot.core.domain.LocalAuthService
import io.soo.springboot.core.support.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class LocalAuthController(
    private val localAuthService: LocalAuthService,
) {

    @PostMapping("/signup")
    fun signUp(
        @RequestBody req: SignUpRequest,
        @RequestHeader(name = "X-Device-Id", required = false) deviceId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<LoginResponse> {
        val r = localAuthService.signUpAndLogin(
            email = req.email,
            rawPassword = req.password,
            nickname = req.nickname,
            deviceId = deviceId,
            request = request,
            response = response,
        )
        return ApiResponse.success(LoginResponse.from(r))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginRequest,
        @RequestHeader(name = "X-Device-Id", required = false) deviceId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<LoginResponse> {
        val r = localAuthService.login(
            email = req.email,
            rawPassword = req.password,
            deviceId = deviceId,
            request = request,
            response = response,
        )
        return ApiResponse.success(LoginResponse.from(r))
    }
}
