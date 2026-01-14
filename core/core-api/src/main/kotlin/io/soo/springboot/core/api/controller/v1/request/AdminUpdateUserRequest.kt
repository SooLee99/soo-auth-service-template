package io.soo.springboot.core.api.controller.v1.request

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AdminUpdateUserRequest(
    val name: String? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,

    // 운영 정책상 허용할 때만(필요 없으면 빼도 됨)
    val emailVerified: Boolean? = null,
)
