package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.services.CertificateService;
import com.eparapheur.db.entities.UserCertificateEntity;
import com.eparapheur.db.repositories.UserCertificateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Path("/certificates")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CertificateManager {

    private static final Logger logger = LoggerFactory.getLogger(CertificateManager.class);

    @Inject
    CertificateService certificateService;

    @Inject
    UserCertificateRepository certificateRepository;

    @POST
    @Path("/upload")
    @Transactional
    @Operation(summary = "Uploader un certificat X509", 
               description = "Permet d'enregistrer un certificat public au format PEM.")
    public Response uploadCertificate(Map<String, String> request) {
        String pemContent = request.get("certificatePem");
        Long accountId = Long.valueOf(request.get("accountId")); // À remplacer par l'ID du token JWT en production

        if (pemContent == null || pemContent.isBlank()) {
            return buildErrorResponse(400, "Le contenu du certificat est requis.");
        }

        try {
            UserCertificateEntity entity = certificateService.parseCertificate(pemContent, accountId);
            certificateService.saveCertificate(entity);

            ApiResponse<UserCertificateEntity> response = new ApiResponse<>();
            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Certificat enregistré avec succès");
            response.setData(entity);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'upload du certificat", e);
            return buildErrorResponse(500, "Erreur lors du traitement du certificat : " + e.getMessage());
        }
    }

    @GET
    @Path("/account/{accountId}")
    @Operation(summary = "Lister les certificats d'un compte", 
               description = "Retourne la liste des certificats actifs pour un utilisateur donné.")
    public Response listCertificates(@PathParam("accountId") Long accountId) {
        List<UserCertificateEntity> certificates = certificateRepository.list("idAccount", accountId);
        
        ApiResponse<List<UserCertificateEntity>> response = new ApiResponse<>();
        response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
        response.setStatus_message("Success");
        response.setData(certificates);
        
        return Response.ok(response).build();
    }

    private Response buildErrorResponse(int statusCode, String message) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(statusCode);
        response.setStatus_message(message);
        return Response.status(statusCode).entity(response).build();
    }
}
