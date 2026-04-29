package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.models.HttpContextStatus;
import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.db.entities.UserSignatureVisualEntity;
import com.eparapheur.db.repositories.UserSignatureVisualRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Path("/signature-visuals")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignatureVisualManager {

    private static final Logger logger = LoggerFactory.getLogger(SignatureVisualManager.class);

    @Inject
    FileStorageService fileStorageService;

    @Inject
    UserSignatureVisualRepository visualRepository;

    @POST
    @Path("/upload")
    @Transactional
    @Operation(summary = "Uploader un visuel de signature", 
               description = "Enregistre une image de signature (Base64) pour un utilisateur.")
    public Response uploadVisual(Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            String label = request.get("label");
            Long accountId = Long.valueOf(request.get("accountId"));
            String visualType = request.get("type"); // drawn, uploaded, typed

            if (base64Image == null || base64Image.isBlank()) {
                return buildErrorResponse(400, "L'image est requise.");
            }

            // Sauvegarde physique
            String fileName = "sig_" + accountId + "_" + System.currentTimeMillis() + ".png";
            String path = fileStorageService.saveBase64File(fileName, base64Image, FileStorageService.StorageType.VISUAL);

            // Calcul du hash de l'image
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            String hash = calculateHash(imageBytes);

            UserSignatureVisualEntity entity = new UserSignatureVisualEntity();
            entity.setIdAccount(accountId);
            entity.setVisualPath(path);
            entity.setVisualHash(hash);
            entity.setVisualType(visualType != null ? visualType : "uploaded");
            entity.setLabel(label != null ? label : "Ma signature");
            entity.setDefault(false);
            entity.setActive(true);

            visualRepository.persist(entity);
            populateVisualUrl(entity);

            ApiResponse<UserSignatureVisualEntity> response = new ApiResponse<>();
            response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
            response.setStatus_message("Visuel de signature enregistré");
            response.setData(entity);

            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'upload du visuel", e);
            return buildErrorResponse(500, "Erreur lors de l'enregistrement du visuel : " + e.getMessage());
        }
    }

    @GET
    @Path("/account/{accountId}")
    public Response listVisuals(@PathParam("accountId") Long accountId) {
        List<UserSignatureVisualEntity> visuals = visualRepository.list("idAccount", accountId);
        visuals.forEach(this::populateVisualUrl);
        
        ApiResponse<List<UserSignatureVisualEntity>> response = new ApiResponse<>();
        response.setStatus_code(HttpContextStatus.SUCCESS_OPERATION);
        response.setStatus_message("Success");
        response.setData(visuals);
        
        return Response.ok(response).build();
    }

    private void populateVisualUrl(UserSignatureVisualEntity entity) {
        if (entity != null && entity.getVisualPath() != null) {
            // Supprimer le préfixe "visuals/" car l'endpoint FileResource le rajoute déjà
            String path = entity.getVisualPath();
            if (path.startsWith("visuals/")) {
                path = path.substring("visuals/".length());
            }
            entity.setVisualUrl("/api/files/visuals/" + path);
        }
    }

    private String calculateHash(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Response buildErrorResponse(int statusCode, String message) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus_code(statusCode);
        response.setStatus_message(message);
        return Response.status(statusCode).entity(response).build();
    }
}
