package com.eparapheur.db.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "account")
public class AccountEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_user", nullable = false)
    private long idUser;

    @Column(name = "id_profil")
    private Long idProfil;

    @Column(name = "login_cmpt", length = 200)
    private String loginCmpt;

    @Column(name = "session_token", length = 100)
    private String sessionToken;

    /**
     * Mot de passe hashé (BCrypt).
     * Masqué des réponses JSON pour éviter toute fuite de données sensibles.
     */
    @JsonIgnore
    @Column(name = "mp_cmpt", nullable = false, length = 255)
    private String mpCmpt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted")
    private Boolean deleted;

    // IMPORTANT : le champ s’appelle "active" (pour coller à tes requêtes JPQL),
    // mais la colonne en BDD reste "is_active"
    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "img_cmpt", length = 200)
    private String imgCmpt;

    @Column(name = "connection_attempt")
    private Integer connectionAttempt;

    @Column(name = "last_connected_at")
    private Timestamp lastConnectedAt;

    // Getters / setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getIdUser() { return idUser; }
    public void setIdUser(long idUser) { this.idUser = idUser; }

    public Long getIdProfil() { return idProfil; }
    public void setIdProfil(Long idProfil) { this.idProfil = idProfil; }

    public String getLoginCmpt() { return loginCmpt; }
    public void setLoginCmpt(String loginCmpt) { this.loginCmpt = loginCmpt; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getMpCmpt() { return mpCmpt; }
    public void setMpCmpt(String mpCmpt) { this.mpCmpt = mpCmpt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getImgCmpt() { return imgCmpt; }
    public void setImgCmpt(String imgCmpt) { this.imgCmpt = imgCmpt; }

    public Integer getConnectionAttempt() { return connectionAttempt; }
    public void setConnectionAttempt(Integer connectionAttempt) { this.connectionAttempt = connectionAttempt; }

    public Timestamp getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(Timestamp lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}