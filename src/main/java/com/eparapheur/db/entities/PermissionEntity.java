package com.eparapheur.db.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@jakarta.persistence.Table(name = "permission", schema = "e-parapheur", catalog = "")
public class PermissionEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @jakarta.persistence.Column(name = "id", nullable = false)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "lib_permis", nullable = false, length = 255)
    private String libPermis;

    public String getLibPermis() {
        return libPermis;
    }

    public void setLibPermis(String libPermis) {
        this.libPermis = libPermis;
    }

    @Basic
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "deleted", nullable = true)
    private Byte deleted;

    public Byte getDeleted() {
        return deleted;
    }

    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }

    @Basic
    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Basic
    @Column(name = "is_active", nullable = true)
    private Byte isActive;

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEntity that = (PermissionEntity) o;
        return id == that.id && Objects.equals(libPermis, that.libPermis) && Objects.equals(code, that.code) && Objects.equals(deleted, that.deleted) && Objects.equals(deletedAt, that.deletedAt) && Objects.equals(isActive, that.isActive) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libPermis, code, deleted, deletedAt, isActive, updatedAt, createdAt);
    }
}
