package com.inventory.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

/**
 * Abstract base entity providing audit fields and soft-delete support.
 * All domain entities extend this class.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy = "system";

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy = "system";

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.createdBy == null) this.createdBy = "system";
        if (this.updatedBy == null) this.updatedBy = "system";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete: marks the entity as deleted instead of physically removing it.
     */
    public void softDelete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted entity.
     */
    public void restore() {
        this.deleted = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Template method for domain validation. Override in subclasses.
     */
    public boolean isValid() {
        return !deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
