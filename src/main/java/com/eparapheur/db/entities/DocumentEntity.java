package com.eparapheur.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "document")
public class DocumentEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "document_path", nullable = false, length = 500)
    private String documentPath;

    @Column(name = "document_size")
    private Long documentSize;

    @Column(name = "document_type", length = 100)
    private String documentType;

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

    @Column(name = "uploaded_by_account", nullable = false)
    private Long uploadedByAccount;

    @ManyToOne
    @JoinColumn(name = "uploaded_by_account", insertable = false, updatable = false)
    private AccountEntity uploader;

    @Column(name = "uploaded_at", nullable = false)
    private Timestamp uploadedAt;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @ManyToMany(mappedBy = "documents")
    private List<ProgramStepEntity> steps;

    public Long getUploadedByAccount() {
        return uploadedByAccount;
    }

    public void setUploadedByAccount(Long uploadedByAccount) {
        this.uploadedByAccount = uploadedByAccount;
    }

    public AccountEntity getUploader() {
        return uploader;
    }

    public void setUploader(AccountEntity uploader) {
        this.uploader = uploader;
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

    public List<ProgramStepEntity> getSteps() {
        return steps;
    }

    public void setSteps(List<ProgramStepEntity> steps) {
        this.steps = steps;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
        if (uploadedAt == null) {
            uploadedAt = new Timestamp(System.currentTimeMillis());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentEntity that = (DocumentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
