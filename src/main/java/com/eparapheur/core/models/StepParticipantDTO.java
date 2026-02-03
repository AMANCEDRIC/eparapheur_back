package com.eparapheur.core.models;

import java.sql.Timestamp;

/**
 * DTO pour un participant d'étape
 */
public class StepParticipantDTO {
    private Long id;
    private Long idStep;
    private Long idAccount;
    private AccountDetailDTO account;  // Utiliser AccountDetailDTO
    private String action;
    private Integer position;
    private Boolean required;
    private String status;
    private Timestamp createdAt;

    public StepParticipantDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdStep() {
        return idStep;
    }

    public void setIdStep(Long idStep) {
        this.idStep = idStep;
    }

    public Long getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Long idAccount) {
        this.idAccount = idAccount;
    }

    public AccountDetailDTO getAccount() {
        return account;
    }

    public void setAccount(AccountDetailDTO account) {
        this.account = account;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

