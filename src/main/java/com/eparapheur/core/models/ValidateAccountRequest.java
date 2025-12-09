package com.eparapheur.core.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la validation d'un compte utilisateur.
 * L'utilisateur reçoit un token par email et doit définir son mot de passe pour activer son compte.
 */
public class ValidateAccountRequest {
    @NotNull(message = "Le token est obligatoire")
    public String token; // Token reçu dans le lien email

    @NotNull(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    public String password;

    public @NotNull String getToken() {
        return token;
    }

    public void setToken(@NotNull String token) {
        this.token = token;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }
}

