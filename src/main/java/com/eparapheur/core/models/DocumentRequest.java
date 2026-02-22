package com.eparapheur.core.models;

import jakarta.validation.constraints.NotBlank;

public class DocumentRequest {
    @NotBlank(message = "Le nom du document est requis")
    private String documentName;
    
    // documentPath n'est plus requis dans la requête, il sera généré par le backend
    private String documentPath;
    
    private Long documentSize;
    
    private String documentType;
    
    @NotBlank(message = "Le contenu binaire du document est requis")
    private String binary;
    
    // Getters/Setters
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    
    public Long getDocumentSize() { return documentSize; }
    public void setDocumentSize(Long documentSize) { this.documentSize = documentSize; }
    
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    
    public String getBinary() { return binary; }
    public void setBinary(String binary) { this.binary = binary; }
}

