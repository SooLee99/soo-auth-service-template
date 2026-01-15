package io.soo.springboot.storage.db.core

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface JwtDenylistRepository : JpaRepository<JwtDenylistEntity, String> {
    fun existsByJti(jti: String): Boolean

    @Modifying
    @Query("delete from JwtDenylistEntity d where d.expiresAt < :now")
    fun deleteExpired(@Param("now") now: Instant): Int
}
