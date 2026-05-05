package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.services.*;
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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
    ProofService proofService;

    @Inject
    OtpService otpService;

    @Inject
    CryptoService cryptoService;

    @Inject
    AuditService auditService;

    @Inject
    EmailService emailService;

    @Inject
    StepParticipantRepository participantRepository;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    UserSignatureVisualRepository visualRepository;

    @Inject
    UserCertificateRepository certificateRepository;

    @Inject
    SignedDocumentRepository signedDocumentRepository;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    ProgramStepRepository stepRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    PersonRepository personRepository;

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

            // NOUVEAU: Support de la signature automatique pour les anciens comptes
            Boolean forceAdvanced = request.get("forceAdvanced") != null && (Boolean) request.get("forceAdvanced");

            // 1. Récupérer le participant
            StepParticipantEntity participant = participantRepository.findById(participantId);
            if (participant == null) return buildErrorResponse(404, "Participant introuvable.");

            // NOUVEAU: Récupérer le paramètre displayIdentity depuis la configuration du programme
            Boolean displayIdentity = false;
            if (participant.getStep() != null && participant.getStep().getProgram() != null) {
                displayIdentity = participant.getStep().getProgram().getDisplayIdentity();
                if (displayIdentity == null) displayIdentity = false;
            }

            // 2. Vérifier l'OTP (Consentement)
            OtpEntity otp = otpService.verifyOtp(participant.getIdAccount(), otpCode, "SIGNATURE");
            if (otp == null) return buildErrorResponse(401, "Code OTP invalide ou expiré.");

            // 3. Récupérer le document original
            DocumentEntity document = documentRepository.findById(documentId);
            if (document == null) return buildErrorResponse(404, "Document introuvable.");

            // 4. Récupérer la version déjà signée si elle existe (cumul)
            SignedDocumentEntity existingSignedDoc = signedDocumentRepository.find("idDocument", documentId).firstResult();
            String inputPath = (existingSignedDoc != null) ? existingSignedDoc.getSignedPath() : document.getDocumentPath();

            // 5. Récupérer le visuel (optionnel)
            UserSignatureVisualEntity visual = (visualId != null) ? visualRepository.findById(visualId) : null;

            // 6. Créer l'acte de signature
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

            // Calcul du hash du fichier d'entrée (avant cette signature)
            action.setDocumentHashBefore(
                    signatureService.calculateDocumentHash(
                            fileStorageService.getAbsolutePath(inputPath).toFile()
                    )
            );

            // 7. Signature cryptographique (si certificat présent ou forcé)
            if (certificateId == null) {
                // Tentative de récupération automatique du certificat par défaut de l'utilisateur
                UserCertificateEntity cert = certificateRepository.find("idAccount = ?1 AND active = true", participant.getIdAccount()).firstResult();
                
                // Si l'utilisateur n'a pas de certificat et qu'on veut de l'avancée (ou réparation automatique)
                if (cert == null) {
                    logger.info("Réparation automatique : Génération d'identité pour le compte {}", participant.getIdAccount());
                    AccountEntity account = accountRepository.findById(participant.getIdAccount());
                    if (account != null) {
                        cryptoService.generateUserIdentity(account);
                        cert = certificateRepository.find("idAccount = ?1 AND active = true", participant.getIdAccount()).firstResult();
                    }
                }
                
                if (cert != null) {
                    certificateId = cert.getId();
                }
            }

            if (certificateId != null) {
                UserCertificateEntity cert = certificateRepository.findById(certificateId);
                if (cert != null) {
                    action.setIdCertificate(certificateId);
                    action.setSignatureLevel(cert.getSignatureLevel());
                    if ("avancee".equals(cert.getSignatureLevel())) {
                        String signatureValue = cryptoService.signHash(action.getDocumentHashBefore(), participant.getIdAccount());
                        action.setSignatureValue(signatureValue);
                        
                        // Lier l'entité clé privée à l'acte
                        UserPrivateKeyEntity keyEntity = cryptoService.getPrivateKeyEntity(participant.getIdAccount());
                        if (keyEntity != null) {
                            action.setIdPrivateKey(keyEntity.getId());
                        }
                    }
                }
            }

            // 8. Signature et Appliquation du visuel (Mode PAdES)
            String resultPath = inputPath;
            if ("avancee".equals(action.getSignatureLevel())) {
                // Récupérer les objets de signature
                PrivateKey privateKey = cryptoService.getPrivateKey(participant.getIdAccount());
                X509Certificate certificate = cryptoService.getX509Certificate(participant.getIdAccount());
                
                resultPath = signatureService.signDocumentPAdES(inputPath, document.getDocumentName(), visual, action, privateKey, certificate, displayIdentity);
            } else if (visual != null) {
                // Fallback signature visuelle simple si non avancée
                // resultPath = signatureService.applySignatureVisual(inputPath, document.getDocumentName(), visual, action);
                // Note: Pour simplifier, on peut tout passer en PAdES si on veut le "Pro" partout
                PrivateKey privateKey = cryptoService.getPrivateKey(participant.getIdAccount());
                X509Certificate certificate = cryptoService.getX509Certificate(participant.getIdAccount());
                resultPath = signatureService.signDocumentPAdES(inputPath, document.getDocumentName(), visual, action, privateKey, certificate, displayIdentity);
            }

            // 8. Enregistrer/Mettre à jour SignedDocumentEntity
            if (existingSignedDoc == null) {
                existingSignedDoc = new SignedDocumentEntity();
                existingSignedDoc.setIdDocument(documentId);
                
                // Trouver le programme et l'étape
                ProgramStepEntity step = stepRepository.findById(participant.getIdStep());
                if (step != null) {
                    existingSignedDoc.setIdStep(step.getId());
                    existingSignedDoc.setIdProgram(step.getIdProgram());
                }
                existingSignedDoc.setSignaturesCount(1);
            } else {
                existingSignedDoc.setSignaturesCount(existingSignedDoc.getSignaturesCount() + 1);
            }
            
            existingSignedDoc.setSignedPath(resultPath);
            File resultFile = fileStorageService.getAbsolutePath(resultPath).toFile();
            existingSignedDoc.setFileSize(resultFile.length());
            existingSignedDoc.setSignedHash(signatureService.calculateDocumentHash(resultFile));
            
            signedDocumentRepository.persist(existingSignedDoc);

            // 9. Enregistrer l'acte
            signatureService.recordSignatureAction(action);

            // 10. Mettre à jour le statut du participant
            participant.setStatus("COMPLETED");
            participantRepository.persist(participant);

            // 11. Logger l'audit
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("actionId", action.getId());
            auditData.put("documentId", documentId);
            auditData.put("signedPath", resultPath);
            auditService.logEvent("signature_completed", auditData, participant.getIdAccount(), 
                                  null, participant.getIdStep(), action.getIpAddress(), action.getUserAgent());

            otpService.markAsUsed(otp);

            // 12. Vérifier si le programme est terminé pour notifier les signataires
            checkAndNotifyProgramCompletion(existingSignedDoc, document);

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

    private void checkAndNotifyProgramCompletion(SignedDocumentEntity signedDoc, DocumentEntity document) {
        if (signedDoc == null) return;
        
        Long programId = signedDoc.getIdProgram();
        long totalParticipants = participantRepository.count("step.idProgram = ?1", programId);
        long completedParticipants = participantRepository.count("step.idProgram = ?1 AND status = 'COMPLETED'", programId);
        
        if (completedParticipants == totalParticipants) {
            logger.info("Programme {} terminé ! Envoi des notifications et génération de preuve...", programId);
            
            // Récupérer le titre du programme
            SignatureProgramEntity program = SignatureProgramEntity.findById(programId);
            String title = (program != null) ? program.getTitle() : "votre document";

            // Génération du dossier de preuve
            try {
                proofService.generateProofFolder(programId);
            } catch (Exception e) {
                logger.error("Erreur lors de la génération du dossier de preuve pour le programme {}", programId, e);
            }
            
            // Notifier tous les participants
            java.util.List<StepParticipantEntity> allParticipants = participantRepository.find("step.idProgram", programId).list();
            for (StepParticipantEntity p : allParticipants) {
                if (p.getAccount() != null) {
                    PersonEntity person = personRepository.findById(p.getAccount().getIdUser());
                    if (person != null && person.getEmailUser() != null) {
                        emailService.sendProgramCompletedEmail(
                            person.getEmailUser(),
                            person.getPrenUser(),
                            title,
                            document.getId(),
                            programId
                        );
                    }
                }
            }
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
