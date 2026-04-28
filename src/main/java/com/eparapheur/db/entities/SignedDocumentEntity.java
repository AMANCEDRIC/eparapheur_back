package com.eparapheur.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "signed_document")
public class SignedDocumentEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_document", nullable = false)
    private Long idDocument;

    @ManyToOne
    @JoinColumn(name = "id_document", insertable = false, updatable = false)
    private DocumentEntity document;

    @Column(name = "id_program", nullable = false)
    private Long idProgram;

    @ManyToOne
    @JoinColumn(name = "id_program", insertable = false, updatable = false)
    private SignatureProgramEntity program;

    @Column(name = "id_step", nullable = false)
    private Long idStep;

    @ManyToOne
    @JoinColumn(name = "id_step", insertable = false, updatable = false)
    private ProgramStepEntity step;

    @Column(name = "signed_path", nullable = false, length = 512)
    private String signedPath;

    @Column(name = "signed_hash", nullable = false, length = 128)
    private String signedHash;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "signatures_count", nullable = false)
    private Integer signaturesCount;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(Long idDocument) {
        this.idDocument = idDocument;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public Long getIdProgram() {
        return idProgram;
    }

    public void setIdProgram(Long idProgram) {
        this.idProgram = idProgram;
    }

    public SignatureProgramEntity getProgram() {
        return program;
    }

    public void setProgram(SignatureProgramEntity program) {
        this.program = program;
    }

    public Long getIdStep() {
        return idStep;
    }

    public void setIdStep(Long idStep) {
        this.idStep = idStep;
    }

    public ProgramStepEntity getStep() {
        return step;
    }

    public void setStep(ProgramStepEntity step) {
        this.step = step;
    }

    public String getSignedPath() {
        return signedPath;
    }

    public void setSignedPath(String signedPath) {
        this.signedPath = signedPath;
    }

    public String getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(String signedHash) {
        this.signedHash = signedHash;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getSignaturesCount() {
        return signaturesCount;
    }

    public void setSignaturesCount(Integer signaturesCount) {
        this.signaturesCount = signaturesCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
        if (signaturesCount == null) {
            signaturesCount = 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignedDocumentEntity that = (SignedDocumentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
