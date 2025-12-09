package com.eparapheur.db.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@jakarta.persistence.Table(name = "person", schema = "e-parapheur", catalog = "")
public class PersonEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @jakarta.persistence.Column(name = "id", nullable = false)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "nom_user", nullable = false, length = 30)
    private String nomUser;

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    @Basic
    @Column(name = "pren_user", nullable = false, length = 50)
    private String prenUser;

    public String getPrenUser() {
        return prenUser;
    }

    public void setPrenUser(String prenUser) {
        this.prenUser = prenUser;
    }

    @Basic
    @Column(name = "tel_user", nullable = true, length = 20)
    private String telUser;

    public String getTelUser() {
        return telUser;
    }

    public void setTelUser(String telUser) {
        this.telUser = telUser;
    }

    @Basic
    @Column(name = "code_user", nullable = true, length = 200)
    private String codeUser;

    public String getCodeUser() {
        return codeUser;
    }

    public void setCodeUser(String codeUser) {
        this.codeUser = codeUser;
    }

    @Basic
    @Column(name = "error_send_confirmation_email", nullable = false)
    private byte errorSendConfirmationEmail;

    public byte getErrorSendConfirmationEmail() {
        return errorSendConfirmationEmail;
    }

    public void setErrorSendConfirmationEmail(byte errorSendConfirmationEmail) {
        this.errorSendConfirmationEmail = errorSendConfirmationEmail;
    }

    @Basic
    @Column(name = "acceptation_CGU", nullable = true)
    private Byte acceptationCgu;

    public Byte getAcceptationCgu() {
        return acceptationCgu;
    }

    public void setAcceptationCgu(Byte acceptationCgu) {
        this.acceptationCgu = acceptationCgu;
    }

    @Basic
    @Column(name = "date_acceptation_CGU", nullable = true)
    private Timestamp dateAcceptationCgu;

    public Timestamp getDateAcceptationCgu() {
        return dateAcceptationCgu;
    }

    public void setDateAcceptationCgu(Timestamp dateAcceptationCgu) {
        this.dateAcceptationCgu = dateAcceptationCgu;
    }

    @Basic
    @Column(name = "genre_user", nullable = true, length = 10)
    private String genreUser;

    public String getGenreUser() {
        return genreUser;
    }

    public void setGenreUser(String genreUser) {
        this.genreUser = genreUser;
    }

    @Basic
    @Column(name = "email_user", nullable = false, length = 50)
    private String emailUser;

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonEntity that = (PersonEntity) o;
        return id == that.id && errorSendConfirmationEmail == that.errorSendConfirmationEmail && Objects.equals(nomUser, that.nomUser) && Objects.equals(prenUser, that.prenUser) && Objects.equals(telUser, that.telUser) && Objects.equals(codeUser, that.codeUser) && Objects.equals(acceptationCgu, that.acceptationCgu) && Objects.equals(dateAcceptationCgu, that.dateAcceptationCgu) && Objects.equals(genreUser, that.genreUser) && Objects.equals(emailUser, that.emailUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nomUser, prenUser, telUser, codeUser, errorSendConfirmationEmail, acceptationCgu, dateAcceptationCgu, genreUser, emailUser);
    }
}
