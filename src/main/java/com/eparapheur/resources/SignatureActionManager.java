package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.services.AuditService;
import com.eparapheur.core.services.OtpService;
import com.eparapheur.core.services.SignatureService;
import com.eparapheur.db.entities.*;
import com.eparapheur.db.repositories.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Path("/signatures")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignatureActionManager {

    private static final Logger logger = LoggerFactory.getLogger(SignatureActionManager.class);

    @Inject
    SignatureService signatureService;

    @Inject
    OtpService otpService;

    @Inject
    AuditService auditService;

    @Inject
    StepParticipantRepository participantRepository;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    UserSignatureVisualRepository visualRepository;

    @Inject
    UserCertificateRepository certificateRepository;

    @POST
    @Path("/execute")
    @Transactional
    @Operation(summary = "Exécuter l'acte de signature", 
               description = "Vérifie l'OTP et applique la signature sur le document.")
    public Response executeSignature(Map<String, Object> request, @Context HttpHeaders headers) {
        try {
            Long participantId = Long.valueOf(request.get("participantId").toString());
            Long documentId = Long.valueOf(request.get("documentId").toString());
            String otpCode = (String) request.get("otp");
            Long visualId = request.get("visualId") != null ? Long.valueOf(request.get("visualId").toString()) : null;
            Long certificateId = request.get("certificateId") != null ? Long.valueOf(request.get("certificateId").toString()) : null;

            // Coordonnées visuelles
            BigDecimal x = request.get("x") != null ? new BigDecimal(request.get("x").toString()) : null;
            BigDecimal y = request.get("y") != null ? new BigDecimal(request.get("y").toString()) : null;
            Integer page = request.get("page") != null ? Integer.valueOf(request.get("page").toString()) : 1;

            // 1. Récupérer le participant
            StepParticipantEntity participant = participantRepository.findById(participantId);
            if (participant == null) return buildErrorResponse(404, "Participant introuvable.");

            // 2. Vérifier l'OTP (Consentement)
            OtpEntity otp = otpService.verifyOtp(participant.getIdAccount(), otpCode, "SIGNATURE");
            if (otp == null) return buildErrorResponse(401, "Code OTP invalide ou expiré.");

            // 3. Récupérer le document
            DocumentEntity document = documentRepository.findById(documentId);
            if (document == null) return buildErrorResponse(404, "Document introuvable.");

            // 4. Récupérer le visuel (optionnel)
            UserSignatureVisualEntity visual = (visualId != null) ? visualRepository.findById(visualId) : null;

            // 5. Créer l'acte de signature
            SignatureActionEntity action = new SignatureActionEntity();
            action.setIdStepParticipant(participantId);
            action.setIdDocument(documentId);
            action.setIdOtp(otp.getIdOtp());
            action.setIdVisual(visualId);
            action.setIdCertificate(certificateId);
            action.setActionType("signature");
            action.setSignaturePage(page);
            action.setSignatureX(x);
            action.setSignatureY(y);
            action.setIpAddress(getIpAddress(headers));
            action.setUserAgent(headers.getHeaderString("User-Agent"));

            // Calcul du hash avant signature
            //action.setDocumentHashBefore(signatureService.calculateDocumentHash(new File(document.getDocumentPath())));

            String fullPath = "uploads/documents/" + document.getDocumentPath();

            action.setDocumentHashBefore(
                    signatureService.calculateDocumentHash(
                            new File(fullPath)
                    )
            );

            // 6. Appliquer le visuel
            if (visual != null) {
                String newPath = signatureService.applySignatureVisual(document, visual, action);
                // On pourrait créer un SignedDocumentEntity ici pour suivre les versions
            }

            // 7. Enregistrer l'acte
            signatureService.recordSignatureAction(action);

            // 8. Mettre à jour le statut du participant
            participant.setStatus("COMPLETED");
            participantRepository.persist(participant);

            // 9. Logger l'audit
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("actionId", action.getId());
            auditData.put("documentId", documentId);
            auditService.logEvent("signature_completed", auditData, participant.getIdAccount(), 
                                  null, participant.getIdStep(), action.getIpAddress(), action.getUserAgent());

            otpService.markAsUsed(otp);

            ApiResponse<SignatureActionEntity> response = new ApiResponse<>();
            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Signature effectuée avec succès");
            response.setData(action);

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution de la signature", e);
            return buildErrorResponse(500, "Erreur lors de la signature : " + e.getMessage());
        }
    }

    private String getIpAddress(HttpHeaders headers) {
        String xForwardedFor = headers.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return "127.0.0.1"; // Valeur par défaut simplifiée
    }

    private Response buildErrorResponse(int statusCode, String message) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(statusCode);
        response.setStatus_message(message);
        return Response.status(statusCode).entity(response).build();
    }
}
