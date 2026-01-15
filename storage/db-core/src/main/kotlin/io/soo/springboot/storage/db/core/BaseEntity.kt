package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.EntityStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_status", nullable = false, length = 20)
    protected var status: EntityStatus = EntityStatus.ACTIVE
        protected set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
        protected set

    fun activate() {
        status = EntityStatus.ACTIVE
    }

    fun delete() {
        status = EntityStatus.DELETED
    }

    fun isActive(): Boolean = status == EntityStatus.ACTIVE
    fun isDeleted(): Boolean = status == EntityStatus.DELETED

    fun getEntityStatus(): EntityStatus = status
}
