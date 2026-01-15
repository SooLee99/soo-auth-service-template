package io.soo.springboot.storage.db.core

import io.soo.springboot.core.enums.EntityStatus
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Version
    @Column(nullable = false)
    var version: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private var status: EntityStatus = EntityStatus.ACTIVE

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.MIN

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.MIN

    fun active() {
        status = EntityStatus.ACTIVE
    }

    fun delete() {
        status = EntityStatus.DELETED
    }

    fun isActive(): Boolean = status == EntityStatus.ACTIVE
    fun isDeleted(): Boolean = status == EntityStatus.DELETED

    fun getEntityStatus(): EntityStatus = status
}
