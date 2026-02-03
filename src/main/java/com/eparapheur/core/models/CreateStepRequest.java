package com.eparapheur.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class CreateStepRequest {
    @NotNull(message = "L'ordre de l'étape est requis")
    @Min(value = 1, message = "L'ordre doit être supérieur à 0")
    private Integer stepOrder;
    
    @NotBlank(message = "Le nom de l'étape est requis")
    private String name;
    
    @NotBlank(message = "Le type d'action est requis")
    @Pattern(regexp = "SIGN|VALIDATION|PARAPHER", message = "Le type d'action doit être SIGN, VALIDATION ou PARAPHER")
    private String actionType;
    
    private String description;
    
    private Boolean required = true;
    
    @NotNull(message = "Les IDs des documents sont requis")
    @Size(min = 1, message = "Au moins un document doit être associé à l'étape")
    private List<Integer> documentIds;
    
    @NotNull(message = "Au moins un participant est requis")
    @Size(min = 1, message = "Au moins un participant est requis")
    @Valid
    private List<ParticipantRequest> participants;
    
    // Getters/Setters
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    
    public List<Integer> getDocumentIds() { return documentIds; }
    public void setDocumentIds(List<Integer> documentIds) { this.documentIds = documentIds; }
    
    public List<ParticipantRequest> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantRequest> participants) { this.participants = participants; }
}

