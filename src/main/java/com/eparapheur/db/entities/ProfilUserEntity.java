package com.eparapheur.db.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@jakarta.persistence.Table(name = "profil_user", schema = "e-parapheur", catalog = "")
public class ProfilUserEntity {
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
    @Column(name = "lib_profil", nullable = false, length = 50)
    private String libProfil;

    public String getLibProfil() {
        return libProfil;
    }

    public void setLibProfil(String libProfil) {
        this.libProfil = libProfil;
    }

    @Basic
    @Column(name = "description", nullable = false, length = -1)
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
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
    @Column(name = "deleted", nullable = true)
    private Byte deleted;

    public Byte getDeleted() {
        return deleted;
    }

    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilUserEntity that = (ProfilUserEntity) o;
        return id == that.id && Objects.equals(libProfil, that.libProfil) && Objects.equals(description, that.description) && Objects.equals(createdAt, that.createdAt) && Objects.equals(isActive, that.isActive) && Objects.equals(deletedAt, that.deletedAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(deleted, that.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libProfil, description, createdAt, isActive, deletedAt, updatedAt, deleted);
    }
}
