package com.eparapheur.core.models;

import java.sql.Timestamp;

/**
 * DTO principal pour un compte avec toutes les informations structurées
 * Exclut les données sensibles : mpCmpt, sessionToken, connectionAttempt, etc.
 */
public class AccountDetailDTO {
    private Long id;
    private String login;           // loginCmpt (email)
    private Boolean active;
    private Boolean deleted;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastConnectedAt;
    
    // Objets imbriqués
    private PersonDTO person;
    private ProfileDTO profile;
    private AvatarDTO avatar;

    public AccountDetailDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getLastConnectedAt() {
        return lastConnectedAt;
    }

    public void setLastConnectedAt(Timestamp lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public AvatarDTO getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarDTO avatar) {
        this.avatar = avatar;
    }
}

