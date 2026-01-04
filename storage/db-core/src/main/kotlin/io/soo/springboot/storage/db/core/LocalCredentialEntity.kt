package io.soo.springboot.storage.db.core

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "local_credential",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_local_credential_user", columnNames = ["user_id"]),
    ],
    indexes = [
        Index(name = "idx_local_credential_user", columnList = "user_id")
    ]
)
@AttributeOverride(
    name = "entityStatus",
    column = Column(name = "entity_status", columnDefinition = "VARCHAR", nullable = false)
)
class LocalCredentialEntity(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "password_hash", nullable = false, length = 200)
    var passwordHash: String,

    @Column(name = "password_updated_at", nullable = false)
    var passwordUpdatedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
