package com.boo.study.common.infrastructure.skeleton

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

interface Identifiable<ID : Serializable> {
    var id: ID?
    val isNew: Boolean
}

interface Traceable {
    var createdAt: LocalDateTime?
    var updatedAt: LocalDateTime?
    var createdBy: String?
    var updatedBy: String?
}


@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity<ID : Serializable> : Identifiable<ID>, Traceable {

    @get:[Id GeneratedValue(strategy = GenerationType.IDENTITY)]
    override var id: ID? = null

    // 데이터 팀 연동을 위해 인덱스 추가된 DDL 반영 됨(idx_createdat)
    @get:[Column(name = "created_at") CreatedDate]
    override var createdAt: LocalDateTime? = null

    // 데이터 팀 연동을 위해 인덱스 추가된 DDL 반영 됨(idx_updatedat)
    @get:[Column(name = "updated_at") LastModifiedDate]
    override var updatedAt: LocalDateTime? = null

    @get:[Column(name = "created_by", length = 100) CreatedBy]
    override var createdBy: String? = null

    @get:[Column(name = "updated_by", length = 100) LastModifiedBy]
    override var updatedBy: String? = null

    @get:[Transient]
    override val isNew: Boolean
        get() = (id == null)

    override fun hashCode(): Int {
        return this.id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun equals(other: Any?): Boolean = when (other) {
        is BaseEntity<*> -> {
            if (isNew && other.isNew) hashCode() == other.hashCode()
            else id == other.id
        }
        else -> false
    }
}

