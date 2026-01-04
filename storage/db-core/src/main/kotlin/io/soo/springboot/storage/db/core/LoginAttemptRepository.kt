package io.soo.springboot.storage.db.core

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface LoginAttemptRepository
    : JpaRepository<LoginAttemptEntity, Long>,
    JpaSpecificationExecutor<LoginAttemptEntity> {

    fun findTop50ByUserIdOrderByCreatedAtDesc(userId: Long): List<LoginAttemptEntity>
}
