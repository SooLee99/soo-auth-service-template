package io.soo.springboot.storage.db.core

import jakarta.persistence.*

@Entity
@Table
class UserAccountEntity(
    @Column
    var email: String? = null,

    @Column
    var nickname: String? = null,

    @Column
    var profileImageUrl: String? = null,

    @Column
    var thumbnailImageUrl: String? = null,
): BaseEntity()
