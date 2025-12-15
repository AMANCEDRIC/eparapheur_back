package com.eparapheur.core.helpers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitaire pour les opérations communes
 */
public class Utilities {
    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    /**
     * Extrait l'accountId du JWT de manière sécurisée
     * @param jwt Le token JWT injecté
     * @return L'accountId ou null si absent/invalide
     */
    public static Long getAccountIdFromJwt(JsonWebToken jwt) {
        try {
            if (jwt == null) {
                logger.warn("JWT est null");
                return null;
            }
            
            Object accountIdClaim = jwt.getClaim("accountId");
            if (accountIdClaim == null) {
                logger.warn("Claim accountId manquant dans le JWT");
                return null;
            }
            
            return Long.valueOf(accountIdClaim.toString());
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'accountId du JWT: {}", e.getMessage(), e);
            return null;
        }
    }
}

