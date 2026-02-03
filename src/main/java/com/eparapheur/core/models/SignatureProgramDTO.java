package com.eparapheur.core.models;

import java.sql.Timestamp;
import java.util.List;

/**
 * DTO pour un programme de signature
 * Évite les références circulaires lors de la sérialisation JSON
 */
public class SignatureProgramDTO {
    private Long id;
    private String title;
    private String description;
    private Long idInitiatorAccount;
    private AccountDetailDTO initiator;  // Utiliser AccountDetailDTO existant
    private String programType;
    private String status;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<ProgramStepDTO> steps;  // DTO pour éviter la référence circulaire

    public SignatureProgramDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getIdInitiatorAccount() {
        return idInitiatorAccount;
    }

    public void setIdInitiatorAccount(Long idInitiatorAccount) {
        this.idInitiatorAccount = idInitiatorAccount;
    }

    public AccountDetailDTO getInitiator() {
        return initiator;
    }

    public void setInitiator(AccountDetailDTO initiator) {
        this.initiator = initiator;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
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

    public List<ProgramStepDTO> getSteps() {
        return steps;
    }

    public void setSteps(List<ProgramStepDTO> steps) {
        this.steps = steps;
    }
}

