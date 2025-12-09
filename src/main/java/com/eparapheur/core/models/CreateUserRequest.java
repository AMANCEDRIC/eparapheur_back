package com.eparapheur.core.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour la création d'un utilisateur par un administrateur.
 * L'utilisateur créé recevra un email avec un lien pour activer son compte et définir son mot de passe.
 */
public class CreateUserRequest {
    @NotNull(message = "Le prénom est obligatoire")
    public String firstName;

    @NotNull(message = "Le nom est obligatoire")
    public String lastName;

    @NotNull(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    public String email;

    public String phone;
    public String gender; // "M" / "F" / autre
    public Long profileId; // Profil assigné par l'admin (optionnel, défaut = AGENT)

    public @NotNull String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotNull String firstName) {
        this.firstName = firstName;
    }

    public @NotNull String getLastName() {
        return lastName;
    }

    public void setLastName(@NotNull String lastName) {
        this.lastName = lastName;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}

