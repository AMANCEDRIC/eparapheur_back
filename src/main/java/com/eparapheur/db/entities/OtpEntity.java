package com.eparapheur.db.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "otp", schema = "e-parapheur", catalog = "")
public class OtpEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_otp", nullable = false)
    private long idOtp;
    @Basic
    @Column(name = "canal", nullable = false)
    private Object canal;
    @Basic
    @Column(name = "action", nullable = false)
    private Object action;
    @Basic
    @Column(name = "otp", nullable = false, length = 6)
    private String otp;
    @Basic
    @Column(name = "id_demanding_account", nullable = false)
    private long idDemandingAccount;
    @Basic
    @Column(name = "validity_date", nullable = false)
    private Timestamp validityDate;
    @Basic
    @Column(name = "used_date", nullable = true)
    private Timestamp usedDate;
    @Basic
    @Column(name = "is_active", nullable = true)
    private Byte isActive;
    @Basic
    @Column(name = "is_deleted", nullable = true)
    private Byte isDeleted;
    @Basic
    @Column(name = "deleted_at", nullable = true)
    private Timestamp deletedAt;
    @Basic
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
    @Basic
    @Column(name = "error_send_otp", nullable = true)
    private Byte errorSendOtp;
    @Basic
    @Column(name = "details", nullable = true, length = 45)
    private String details;

    public long getIdOtp() {
        return idOtp;
    }

    public void setIdOtp(long idOtp) {
        this.idOtp = idOtp;
    }

    public Object getCanal() {
        return canal;
    }

    public void setCanal(Object canal) {
        this.canal = canal;
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public long getIdDemandingAccount() {
        return idDemandingAccount;
    }

    public void setIdDemandingAccount(long idDemandingAccount) {
        this.idDemandingAccount = idDemandingAccount;
    }

    public Timestamp getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(Timestamp validityDate) {
        this.validityDate = validityDate;
    }

    public Timestamp getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(Timestamp usedDate) {
        this.usedDate = usedDate;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Byte getErrorSendOtp() {
        return errorSendOtp;
    }

    public void setErrorSendOtp(Byte errorSendOtp) {
        this.errorSendOtp = errorSendOtp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtpEntity otpEntity = (OtpEntity) o;
        return idOtp == otpEntity.idOtp && idDemandingAccount == otpEntity.idDemandingAccount && Objects.equals(canal, otpEntity.canal) && Objects.equals(action, otpEntity.action) && Objects.equals(otp, otpEntity.otp) && Objects.equals(validityDate, otpEntity.validityDate) && Objects.equals(usedDate, otpEntity.usedDate) && Objects.equals(isActive, otpEntity.isActive) && Objects.equals(isDeleted, otpEntity.isDeleted) && Objects.equals(deletedAt, otpEntity.deletedAt) && Objects.equals(createdAt, otpEntity.createdAt) && Objects.equals(errorSendOtp, otpEntity.errorSendOtp) && Objects.equals(details, otpEntity.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOtp, canal, action, otp, idDemandingAccount, validityDate, usedDate, isActive, isDeleted, deletedAt, createdAt, errorSendOtp, details);
    }
}
