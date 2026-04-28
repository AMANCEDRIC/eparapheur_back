package com.eparapheur.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "user_certificate")
public class UserCertificateEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_account", nullable = false)
    private Long idAccount;

    @ManyToOne
    @JoinColumn(name = "id_account", insertable = false, updatable = false)
    private AccountEntity account;

    @Column(name = "serial_number", nullable = false, length = 128, unique = true)
    private String serialNumber;

    @Column(name = "issuer", nullable = false, length = 512)
    private String issuer;

    @Column(name = "subject", nullable = false, length = 512)
    private String subject;

    @Column(name = "certificate_pem", nullable = false, columnDefinition = "TEXT")
    private String certificatePem;

    @Column(name = "public_key_fingerprint", nullable = false, length = 128)
    private String publicKeyFingerprint;

    @Column(name = "valid_from", nullable = false)
    private Timestamp validFrom;

    @Column(name = "valid_until", nullable = false)
    private Timestamp validUntil;

    @Column(name = "certificate_type", nullable = false)
    private String certificateType;

    @Column(name = "signature_level", nullable = false)
    private String signatureLevel;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "hostname", length = 255)
    private String hostname;

    @Column(name = "generation_state", nullable = false)
    private String generationState;

    @Column(name = "renew_certificate", nullable = false)
    private Boolean renewCertificate;

    @Column(name = "revoked_at")
    private Timestamp revokedAt;

    @Column(name = "revocation_reason", length = 255)
    private String revocationReason;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Long idAccount) {
        this.idAccount = idAccount;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getPublicKeyFingerprint() {
        return publicKeyFingerprint;
    }

    public void setPublicKeyFingerprint(String publicKeyFingerprint) {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }

    public Timestamp getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    public Timestamp getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Timestamp validUntil) {
        this.validUntil = validUntil;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getSignatureLevel() {
        return signatureLevel;
    }

    public void setSignatureLevel(String signatureLevel) {
        this.signatureLevel = signatureLevel;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getGenerationState() {
        return generationState;
    }

    public void setGenerationState(String generationState) {
        this.generationState = generationState;
    }

    public Boolean getRenewCertificate() {
        return renewCertificate;
    }

    public void setRenewCertificate(Boolean renewCertificate) {
        this.renewCertificate = renewCertificate;
    }

    public Timestamp getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        if (active == null) {
            active = true;
        }
        if (renewCertificate == null) {
            renewCertificate = false;
        }
        if (generationState == null) {
            generationState = "pending";
        }
        if (signatureLevel == null) {
            signatureLevel = "simple";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCertificateEntity that = (UserCertificateEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
