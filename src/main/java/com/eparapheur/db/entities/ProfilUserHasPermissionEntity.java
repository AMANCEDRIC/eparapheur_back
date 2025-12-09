package com.eparapheur.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "profil_user_has_permission", schema = "e-parapheur")
@IdClass(ProfilUserHasPermissionEntityPK.class)
public class ProfilUserHasPermissionEntity {

    @Id
    @Column(name = "id_user", nullable = false)
    private long idUser;

    @Id
    @Column(name = "id_permission", nullable = false)
    private long idPermission;

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public long getIdPermission() {
        return idPermission;
    }

    public void setIdPermission(long idPermission) {
        this.idPermission = idPermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilUserHasPermissionEntity that = (ProfilUserHasPermissionEntity) o;
        return idUser == that.idUser && idPermission == that.idPermission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idPermission);
    }
}
