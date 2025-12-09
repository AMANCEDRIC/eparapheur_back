package com.eparapheur.db.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clé primaire composite pour {@link ProfilUserHasPermissionEntity}.
 * Doit contenir exactement les mêmes propriétés que celles annotées avec {@code @Id}
 * dans l'entité, avec les mêmes types.
 */
public class ProfilUserHasPermissionEntityPK implements Serializable {

    private Long idUser;
    private Long idPermission;

    public ProfilUserHasPermissionEntityPK() {
    }

    public ProfilUserHasPermissionEntityPK(Long idUser, Long idPermission) {
        this.idUser = idUser;
        this.idPermission = idPermission;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdPermission() {
        return idPermission;
    }

    public void setIdPermission(Long idPermission) {
        this.idPermission = idPermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfilUserHasPermissionEntityPK that)) return false;
        return Objects.equals(idUser, that.idUser) &&
                Objects.equals(idPermission, that.idPermission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idPermission);
    }
}
