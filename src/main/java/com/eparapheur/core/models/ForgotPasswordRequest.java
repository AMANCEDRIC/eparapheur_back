package com.eparapheur.core.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour la demande de réinitialisation de mot de passe.
 * L'utilisateur fournit son email pour recevoir un lien de réinitialisation.
 */
public class ForgotPasswordRequest {
    
    @NotNull(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    public String email;

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }
}

