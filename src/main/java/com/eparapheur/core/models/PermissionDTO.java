package com.eparapheur.core.models;

/**
 * DTO pour les permissions
 */
public class PermissionDTO {
    private Long id;
    private String name;        // libPermis
    private String code;         // code

    public PermissionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

