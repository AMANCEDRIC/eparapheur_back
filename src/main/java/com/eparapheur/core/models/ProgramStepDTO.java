package com.eparapheur.core.models;

import java.sql.Timestamp;
import java.util.List;

/**
 * DTO pour une étape de programme
 * Sans référence circulaire au program parent
 */
public class ProgramStepDTO {
    private Long id;
    private Long idProgram;  // Juste l'ID, pas l'objet complet
    private Integer stepOrder;
    private String actionType;
    private String name;
    private String description;
    private Boolean required;
    private String status;
    private Timestamp createdAt;
    private List<StepParticipantDTO> participants;
    private List<DocumentDTO> documents;

    public ProgramStepDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdProgram() {
        return idProgram;
    }

    public void setIdProgram(Long idProgram) {
        this.idProgram = idProgram;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
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

    public List<StepParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<StepParticipantDTO> participants) {
        this.participants = participants;
    }

    public List<DocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDTO> documents) {
        this.documents = documents;
    }
}

