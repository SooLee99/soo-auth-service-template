package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.AuthProvider
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object UserAccountSpecs {

    fun filter(
        q: String?,
        email: String?,
        nickname: String?,
        provider: AuthProvider?,
        suspended: Boolean?,
        createdFrom: LocalDateTime?,
        createdTo: LocalDateTime?,
    ): Specification<UserAccountEntity> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            // ✅ 통합 검색(q): 숫자면 userId, 아니면 email/nickname like
            if (!q.isNullOrBlank()) {
                val keyword = q.trim()
                val idLong = keyword.toLongOrNull()
                if (idLong != null) {
                    predicates += cb.equal(root.get<Long>("id"), idLong)
                } else {
                    val like = "%${keyword.lowercase()}%"
                    predicates += cb.or(
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("nickname")), like),
                    )
                }
            }

            if (!email.isNullOrBlank()) {
                predicates += cb.equal(cb.lower(root.get("email")), email.trim().lowercase())
            }

            if (!nickname.isNullOrBlank()) {
                val like = "%${nickname.trim().lowercase()}%"
                predicates += cb.like(cb.lower(root.get("nickname")), like)
            }

            // ✅ 계정 정지 여부
            if (suspended != null) {
                if (suspended) {
                    predicates += cb.isNotNull(root.get<LocalDateTime>("suspendedAt"))
                } else {
                    predicates += cb.isNull(root.get<LocalDateTime>("suspendedAt"))
                }
            }

            // ✅ 가입일(createdAt) 범위
            if (createdFrom != null) predicates += cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom)
            if (createdTo != null) predicates += cb.lessThanOrEqualTo(root.get("createdAt"), createdTo)

            // ✅ provider 필터: OAuthIdentityEntity에 해당 provider가 존재하는 유저만
            if (provider != null) {
                val sq = query?.subquery(Long::class.java)
                val idRoot = sq?.from(OAuthIdentityEntity::class.java)
                sq?.select(idRoot?.get("userId"))
                    ?.where(
                        cb.equal(idRoot?.get<AuthProvider>("provider"), provider),
                        cb.equal(idRoot?.get<Long>("userId"), root.get<Long>("id")),
                    )
                predicates += cb.exists(sq)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
