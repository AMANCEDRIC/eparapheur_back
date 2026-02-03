package com.eparapheur.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class CreateSignatureProgramRequest {
    @NotBlank(message = "Le code OTP est requis")
    private String otp;
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;
    
    @NotBlank(message = "Le label est requis")
    private String label;
    
    private String description;
    
    private String programType = "INTERNAL_FLOW";
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Au moins un document est requis")
    @Size(min = 1, message = "Au moins un document est requis")
    @Valid
    private List<DocumentRequest> documents;
    
    @NotNull(message = "Au moins une étape est requise")
    @Size(min = 1, message = "Au moins une étape est requise")
    @Valid
    private List<CreateStepRequest> steps;
    
    // Getters/Setters
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getProgramType() { return programType; }
    public void setProgramType(String programType) { this.programType = programType; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public List<DocumentRequest> getDocuments() { return documents; }
    public void setDocuments(List<DocumentRequest> documents) { this.documents = documents; }
    
    public List<CreateStepRequest> getSteps() { return steps; }
    public void setSteps(List<CreateStepRequest> steps) { this.steps = steps; }
}

