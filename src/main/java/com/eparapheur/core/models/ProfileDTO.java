package com.eparapheur.core.models;

import java.util.List;

/**
 * DTO pour le profil utilisateur avec permissions
 * Basé sur ProfilUserEntity
 */
public class ProfileDTO {
    private Long id;
    private String code;           // libProfil (ex: "ADMIN", "AGENT", "MANAGER")
    private String name;            // libProfil (même valeur que code)
    private String description;     // description du profil
    private List<PermissionDTO> permissions;  // Permissions associées au profil

    public ProfileDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }
}

