package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.AuthProvider
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_account",
    indexes = [
        Index(name = "idx_user_account_email", columnList = "email"),
        Index(name = "idx_user_account_last_login_at", columnList = "last_login_at"),
    ],
)
@AttributeOverride(
    name = "status",
    column = Column(name = "status", columnDefinition = "VARCHAR", nullable = false),
)
class UserAccountEntity(

    @Column(length = 320)
    var email: String? = null,

    @Column
    var emailVerified: Boolean? = null,

    @Column(length = 150)
    var nickname: String? = null,

    @Column(length = 100)
    var name: String? = null,

    @Column(length = 1000)
    var profileImageUrl: String? = null,

    @Column(length = 1000)
    var thumbnailImageUrl: String? = null,

    @Column
    var birthdate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "last_login_provider", length = 30)
    var lastLoginProvider: AuthProvider? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null,

    @Column(name = "suspended_at")
    var suspendedAt: LocalDateTime? = null,

    @Column(name = "suspended_until")
    var suspendedUntil: LocalDateTime? = null,

    @Column(name = "suspended_reason", length = 500)
    var suspendedReason: String? = null,
) : BaseEntity() {

    fun isSuspended(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val until = suspendedUntil
        return suspendedAt != null && (until == null || now.isBefore(until))
    }
}
