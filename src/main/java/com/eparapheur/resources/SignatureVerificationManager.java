package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.services.CryptoService;
import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.core.services.SignatureService;
import com.eparapheur.db.entities.SignatureActionEntity;
import com.eparapheur.db.entities.UserCertificateEntity;
import com.eparapheur.db.repositories.SignatureActionRepository;
import com.eparapheur.db.repositories.UserCertificateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Path("/signatures/verification")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignatureVerificationManager {

    private static final Logger logger = LoggerFactory.getLogger(SignatureVerificationManager.class);

    @Inject
    CryptoService cryptoService;

    @Inject
    SignatureService signatureService;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    SignatureActionRepository signatureActionRepository;

    @Inject
    UserCertificateRepository certificateRepository;

    @POST
    @Path("/verify-action/{actionId}")
    @Operation(summary = "Vérifier l'intégrité d'un acte de signature", 
               description = "Vérifie si le document n'a pas été modifié depuis la signature cryptographique.")
    public Response verifySignatureAction(@PathParam("actionId") Long actionId) {
        try {
            SignatureActionEntity action = signatureActionRepository.findById(actionId);
            if (action == null) {
                return buildErrorResponse(404, "Acte de signature introuvable.");
            }

            if (action.getSignatureValue() == null) {
                return buildErrorResponse(400, "Cet acte ne possède pas de signature cryptographique (signature simple uniquement).");
            }

            if (action.getIdCertificate() == null) {
                return buildErrorResponse(400, "Aucun certificat associé à cet acte.");
            }

            UserCertificateEntity cert = certificateRepository.findById(action.getIdCertificate());
            if (cert == null) {
                return buildErrorResponse(404, "Certificat introuvable.");
            }

            // 1. Récupérer le hash original qui a été signé
            String signedHash = action.getDocumentHashBefore();

            // 2. Vérifier la signature contre ce hash avec la clé publique
            boolean isAuthentic = cryptoService.verifySignature(
                    signedHash, 
                    action.getSignatureValue(), 
                    extractPublicKeyFromPem(cert.getCertificatePem())
            );

            Map<String, Object> result = new HashMap<>();
            result.put("isAuthentic", isAuthentic);
            result.put("signedBy", cert.getSubject());
            result.put("signedAt", action.getSignedAt());
            result.put("hashSigned", signedHash);
            
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message(isAuthentic ? "Signature authentique" : "Signature invalide ou falsifiée");
            response.setData(result);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de la signature", e);
            return buildErrorResponse(500, "Erreur de vérification : " + e.getMessage());
        }
    }

    private String extractPublicKeyFromPem(String pem) {
        // Enlève les headers et footers PEM pour obtenir le Base64 pur (simplifié)
        return pem.replace("-----BEGIN CERTIFICATE-----", "")
                  .replace("-----END CERTIFICATE-----", "")
                  .replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");
    }

    private Response buildErrorResponse(int statusCode, String message) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(statusCode);
        response.setStatus_message(message);
        return Response.status(statusCode).entity(response).build();
    }
}
