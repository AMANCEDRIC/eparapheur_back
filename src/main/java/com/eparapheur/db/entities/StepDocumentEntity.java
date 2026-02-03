package com.eparapheur.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "step_document")
@IdClass(StepDocumentEntityPK.class)
public class StepDocumentEntity extends PanacheEntityBase {
    @Id
    @Column(name = "id_step", nullable = false)
    private long idStep;

    @Id
    @Column(name = "id_document", nullable = false)
    private long idDocument;

    @ManyToOne
    @JoinColumn(name = "id_step", insertable = false, updatable = false)
    private ProgramStepEntity step;

    @ManyToOne
    @JoinColumn(name = "id_document", insertable = false, updatable = false)
    private DocumentEntity document;

    public long getIdStep() {
        return idStep;
    }

    public void setIdStep(long idStep) {
        this.idStep = idStep;
    }

    public long getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(long idDocument) {
        this.idDocument = idDocument;
    }

    public ProgramStepEntity getStep() {
        return step;
    }

    public void setStep(ProgramStepEntity step) {
        this.step = step;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepDocumentEntity that = (StepDocumentEntity) o;
        return idStep == that.idStep && idDocument == that.idDocument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStep, idDocument);
    }
}
