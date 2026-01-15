package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.AuthProvider
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OAuthIdentityRepository : JpaRepository<OAuthIdentityEntity, Long> {

    fun findAllByUserId(userId: Long): List<OAuthIdentityEntity>
    fun findAllByUserIdIn(userIds: Collection<Long>): List<OAuthIdentityEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select o from OAuthIdentityEntity o " +
            "where o.provider = :provider and o.providerUserId = :providerUserId",
    )
    fun lockByProviderAndProviderUserId(
        @Param("provider") provider: AuthProvider,
        @Param("providerUserId") providerUserId: String,
    ): OAuthIdentityEntity?
}
