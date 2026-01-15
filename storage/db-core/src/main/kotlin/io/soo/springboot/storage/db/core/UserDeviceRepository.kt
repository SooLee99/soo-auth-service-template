package io.soo.springboot.storage.db.core

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserDeviceRepository : JpaRepository<UserDeviceEntity, Long> {

    fun findByUserIdAndDeviceId(userId: Long, deviceId: String): UserDeviceEntity?
    fun findAllByUserId(userId: Long): List<UserDeviceEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from UserDeviceEntity d where d.userId = :userId and d.deviceId = :deviceId")
    fun lockByUserIdAndDeviceId(
        @Param("userId") userId: Long,
        @Param("deviceId") deviceId: String,
    ): UserDeviceEntity?

    /**
     * ✅ userId별 디바이스 개수 집계 (group by)
     */
    @Query(
        """
        select d.userId as userId, count(d) as cnt
        from UserDeviceEntity d
        where d.userId in :userIds
        group by d.userId
        """,
    )
    fun countByUserIdIn(@Param("userIds") userIds: Collection<Long>): List<UserIdCountRow>
}

interface UserIdCountRow {
    val userId: Long
    val cnt: Long
}
