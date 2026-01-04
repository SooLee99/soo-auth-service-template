package io.soo.springboot.storage.db.core

import org.springframework.data.jpa.repository.JpaRepository

interface LocalCredentialRepository : JpaRepository<LocalCredentialEntity, Long> {
    fun findByUserId(userId: Long): LocalCredentialEntity?
    fun existsByUserId(userId: Long): Boolean
}
