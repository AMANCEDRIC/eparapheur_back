package com.eparapheur.core.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la réinitialisation du mot de passe.
 * L'utilisateur fournit le token reçu par email et son nouveau mot de passe.
 */
public class ResetPasswordRequest {
    
    @NotNull(message = "Le token est obligatoire")
    public String token; // Token reçu dans le lien email

    @NotNull(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
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

