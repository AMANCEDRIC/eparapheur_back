package com.eparapheur.db.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "signature_action")
public class SignatureActionEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_step_participant", nullable = false)
    private Long idStepParticipant;

    @ManyToOne
    @JoinColumn(name = "id_step_participant", insertable = false, updatable = false)
    private StepParticipantEntity participant;

    @Column(name = "id_document", nullable = false)
    private Long idDocument;

    @ManyToOne
    @JoinColumn(name = "id_document", insertable = false, updatable = false)
    private DocumentEntity document;

    @Column(name = "id_certificate")
    private Long idCertificate;

    @ManyToOne
    @JoinColumn(name = "id_certificate", insertable = false, updatable = false)
    private UserCertificateEntity certificate;

    @Column(name = "id_visual")
    private Long idVisual;

    @ManyToOne
    @JoinColumn(name = "id_visual", insertable = false, updatable = false)
    private UserSignatureVisualEntity visual;

    @Column(name = "id_private_key")
    private Long idPrivateKey;

    @ManyToOne
    @JoinColumn(name = "id_private_key", insertable = false, updatable = false)
    private UserPrivateKeyEntity privateKey;

    @Column(name = "id_otp", nullable = false)
    private Long idOtp;

    @ManyToOne
    @JoinColumn(name = "id_otp", insertable = false, updatable = false)
    private OtpEntity otp;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "signature_level", nullable = false)
    private String signatureLevel;

    @Column(name = "document_hash_before", nullable = false, length = 128)
    private String documentHashBefore;

    @Column(name = "signature_value", columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signature_page")
    private Integer signaturePage;

    @Column(name = "signature_x")
    private BigDecimal signatureX;

    @Column(name = "signature_y")
    private BigDecimal signatureY;

    @Column(name = "signature_width")
    private BigDecimal signatureWidth;

    @Column(name = "signature_height")
    private BigDecimal signatureHeight;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "geolocation", length = 128)
    private String geolocation;

    @Column(name = "signed_at", nullable = false)
    private Timestamp signedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdStepParticipant() {
        return idStepParticipant;
    }

    public void setIdStepParticipant(Long idStepParticipant) {
        this.idStepParticipant = idStepParticipant;
    }

    public StepParticipantEntity getParticipant() {
        return participant;
    }

    public void setParticipant(StepParticipantEntity participant) {
        this.participant = participant;
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

    public Long getIdCertificate() {
        return idCertificate;
    }

    public void setIdCertificate(Long idCertificate) {
        this.idCertificate = idCertificate;
    }

    public UserCertificateEntity getCertificate() {
        return certificate;
    }

    public void setCertificate(UserCertificateEntity certificate) {
        this.certificate = certificate;
    }

    public Long getIdVisual() {
        return idVisual;
    }

    public void setIdVisual(Long idVisual) {
        this.idVisual = idVisual;
    }

    public UserSignatureVisualEntity getVisual() {
        return visual;
    }

    public void setVisual(UserSignatureVisualEntity visual) {
        this.visual = visual;
    }

    public Long getIdPrivateKey() {
        return idPrivateKey;
    }

    public void setIdPrivateKey(Long idPrivateKey) {
        this.idPrivateKey = idPrivateKey;
    }

    public UserPrivateKeyEntity getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(UserPrivateKeyEntity privateKey) {
        this.privateKey = privateKey;
    }

    public Long getIdOtp() {
        return idOtp;
    }

    public void setIdOtp(Long idOtp) {
        this.idOtp = idOtp;
    }

    public OtpEntity getOtp() {
        return otp;
    }

    public void setOtp(OtpEntity otp) {
        this.otp = otp;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getSignatureLevel() {
        return signatureLevel;
    }

    public void setSignatureLevel(String signatureLevel) {
        this.signatureLevel = signatureLevel;
    }

    public String getDocumentHashBefore() {
        return documentHashBefore;
    }

    public void setDocumentHashBefore(String documentHashBefore) {
        this.documentHashBefore = documentHashBefore;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public Integer getSignaturePage() {
        return signaturePage;
    }

    public void setSignaturePage(Integer signaturePage) {
        this.signaturePage = signaturePage;
    }

    public BigDecimal getSignatureX() {
        return signatureX;
    }

    public void setSignatureX(BigDecimal signatureX) {
        this.signatureX = signatureX;
    }

    public BigDecimal getSignatureY() {
        return signatureY;
    }

    public void setSignatureY(BigDecimal signatureY) {
        this.signatureY = signatureY;
    }

    public BigDecimal getSignatureWidth() {
        return signatureWidth;
    }

    public void setSignatureWidth(BigDecimal signatureWidth) {
        this.signatureWidth = signatureWidth;
    }

    public BigDecimal getSignatureHeight() {
        return signatureHeight;
    }

    public void setSignatureHeight(BigDecimal signatureHeight) {
        this.signatureHeight = signatureHeight;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }

    public Timestamp getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Timestamp signedAt) {
        this.signedAt = signedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @PrePersist
    protected void onCreate() {
        if (signedAt == null) {
            signedAt = new Timestamp(System.currentTimeMillis());
        }
        if (status == null) {
            status = "completed";
        }
        if (signatureLevel == null) {
            signatureLevel = "simple";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureActionEntity that = (SignatureActionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
