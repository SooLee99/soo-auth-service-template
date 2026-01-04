package io.soo.springboot.storage.db.core

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserSessionMapRepository : JpaRepository<UserSessionMapEntity, Long> {

    fun findBySessionIdAndRevokedAtIsNull(sessionId: String): UserSessionMapEntity?
    fun findBySessionId(sessionId: String): UserSessionMapEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from UserSessionMapEntity m where m.sessionId = :sessionId")
    fun lockBySessionId(@Param("sessionId") sessionId: String): UserSessionMapEntity?

    @Query(
        """
        select m.sessionId
        from UserSessionMapEntity m
        where m.userId = :userId and m.revokedAt is null
        """
    )
    fun findActiveSessionIdsByUserId(@Param("userId") userId: Long): List<String>

    @Query(
        """
        select m.sessionId
        from UserSessionMapEntity m
        where m.userId = :userId and m.deviceId = :deviceId and m.revokedAt is null
        """
    )
    fun findActiveSessionIdsByUserIdAndDeviceId(
        @Param("userId") userId: Long,
        @Param("deviceId") deviceId: String,
    ): List<String>

    fun findAllByUserIdAndRevokedAtIsNull(userId: Long): List<UserSessionMapEntity>
    fun findAllByUserIdAndDeviceIdAndRevokedAtIsNull(userId: Long, deviceId: String): List<UserSessionMapEntity>

    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<UserSessionMapEntity>
    fun findAllByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId: Long): List<UserSessionMapEntity>

    @Query(
        """
        select count(m)
        from UserSessionMapEntity m
        where m.userId = :userId and m.revokedAt is null
        """
    )
    fun countActiveByUserId(@Param("userId") userId: Long): Long
}
