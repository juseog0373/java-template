package com.nexacode.template.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @Comment("생성일자(UTC)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Comment("수정일자(UTC)")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @Comment("삭제일자(UTC)")
    private LocalDateTime deletedAt;
}
