package com.eparapheur.db.entities;

import java.io.Serializable;
import java.util.Objects;

public class StepDocumentEntityPK implements Serializable {
    private long idStep;
    private long idDocument;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepDocumentEntityPK that = (StepDocumentEntityPK) o;
        return idStep == that.idStep && idDocument == that.idDocument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStep, idDocument);
    }
}
