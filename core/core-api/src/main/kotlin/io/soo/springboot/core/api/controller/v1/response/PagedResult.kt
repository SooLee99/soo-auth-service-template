package io.soo.springboot.core.api.controller.v1.response

data class PagedResult<T>(
    val items: List<T>,
    val page: PageMetaDto,
)
