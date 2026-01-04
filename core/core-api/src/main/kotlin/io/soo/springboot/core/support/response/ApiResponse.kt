package io.soo.springboot.core.support.response

import io.soo.springboot.core.api.controller.v1.response.PageMetaDto
import io.soo.springboot.core.support.error.ErrorMessage
import io.soo.springboot.core.support.error.ErrorType

data class ApiResponse<T> private constructor(
    val result: ResultType,
    val data: T? = null,
    val page: PageMetaDto? = null,
    val error: ErrorMessage? = null,
) {
    companion object {
        fun success(): ApiResponse<Unit> =
            ApiResponse(ResultType.SUCCESS)

        fun <S> success(data: S): ApiResponse<S> =
            ApiResponse(ResultType.SUCCESS, data = data)

        fun <S> success(data: S, page: PageMetaDto): ApiResponse<S> =
            ApiResponse(ResultType.SUCCESS, data = data, page = page)

        fun <S> error(error: ErrorType, errorData: Any? = null): ApiResponse<S> =
            ApiResponse(ResultType.ERROR, error = ErrorMessage(error, errorData))
    }
}
