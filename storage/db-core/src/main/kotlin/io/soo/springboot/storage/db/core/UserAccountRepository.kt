package io.soo.springboot.storage.db.core

import org.springframework.data.jpa.repository.JpaRepository

interface UserAccountRepository : JpaRepository<UserAccountEntity, Long> {
    fun findByEmail(email: String): UserAccountEntity?
}
