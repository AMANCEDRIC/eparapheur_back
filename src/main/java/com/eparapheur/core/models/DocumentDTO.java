package com.eparapheur.core.models;

import java.sql.Timestamp;

/**
 * DTO pour un document
 */
public class DocumentDTO {
    private Long id;
    private String documentName;
    private String documentPath;
    private Long documentSize;
    private String documentType;
    private Long uploadedByAccount;
    private Timestamp uploadedAt;
    private Timestamp createdAt;

    public DocumentDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public Long getDocumentSize() {
        return documentSize;
    }

    public void setDocumentSize(Long documentSize) {
        this.documentSize = documentSize;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getUploadedByAccount() {
        return uploadedByAccount;
    }

    public void setUploadedByAccount(Long uploadedByAccount) {
        this.uploadedByAccount = uploadedByAccount;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

