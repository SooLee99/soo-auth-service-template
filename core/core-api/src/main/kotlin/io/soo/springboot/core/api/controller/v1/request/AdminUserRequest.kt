package io.soo.springboot.core.api.controller.v1.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminSuspendUserRequest(
    @field:NotBlank(message = "정지 사유는 필수입니다.")
    val reason: String,

    // null이면 "무기한 정지"로 처리(정책에 맞게)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val until: LocalDateTime? = null,
)

data class AdminUpdateUserRequest(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val thumbnailImageUrl: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,

    // 운영 정책상 허용할 때만(필요 없으면 빼도 됨)
    val emailVerified: Boolean? = null,
)
