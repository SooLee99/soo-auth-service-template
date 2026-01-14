package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.AuthProvider
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "oauth_identity",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_oauth_identity_provider_user",
            columnNames = ["provider", "provider_user_id"],
        ),
    ],
    indexes = [Index(name = "idx_oauth_identity_user_id", columnList = "user_id")],
)
@AttributeOverride(
    name = "status",
    column = Column(name = "status", columnDefinition = "VARCHAR", nullable = false),
)
class OAuthIdentityEntity(

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    var provider: AuthProvider,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    var providerUserId: String,

    @Lob
    @Column(name = "raw_attributes_json", columnDefinition = "TEXT")
    var rawAttributesJson: String? = null,

    ) : BaseEntity()
