package com.eparapheur.core.models;

import jakarta.validation.constraints.NotNull;

public class ParticipantRequest {
    @NotNull(message = "L'ID du compte est requis")
    private Long accountId;
    
    private Integer position = 0;
    
    private Boolean required = true;
    
    // Getters/Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
}

