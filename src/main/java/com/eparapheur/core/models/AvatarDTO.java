package com.eparapheur.core.models;

import java.sql.Timestamp;

/**
 * DTO pour l'avatar de l'utilisateur
 * Basé sur AccountEntity.imgCmpt
 */
public class AvatarDTO {
    private Long id;
    private String url;             // imgCmpt de AccountEntity
    private Timestamp createdAt;

    public AvatarDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

