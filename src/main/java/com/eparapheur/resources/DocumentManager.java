package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.db.entities.DocumentEntity;
import com.eparapheur.db.repositories.DocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

@Path("/documents")
@ApplicationScoped
public class DocumentManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);
    
    @Inject
    DocumentRepository documentRepository;
    
    @Inject
    FileStorageService fileStorageService;
    
    /**
     * Télécharge un document par son ID
     * Retourne le fichier binaire avec le bon Content-Type
     */
    @GET
    @Path("{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Télécharger un document",
               description = "Retourne le fichier binaire d'un document par son ID")
    public Response download(@PathParam("id") Long id) {
        try {
            // 1. Récupérer le document
            DocumentEntity document = documentRepository.findById(id);
            
            if (document == null) {
                ApiResponse<Object> errorResponse = new ApiResponse<>();
                errorResponse.setStatus_code(404);
                errorResponse.setStatus_message("Document non trouvé");
                return Response.status(404)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
            }
            
            // 2. Récupérer le contenu du fichier depuis le stockage
            byte[] fileContent = fileStorageService.getFileContent(document.getDocumentPath());
            
            // 3. Déterminer le Content-Type
            String contentType = document.getDocumentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            
            // 4. Retourner le fichier avec les headers appropriés
            return Response.ok(new ByteArrayInputStream(fileContent))
                .type(contentType)
                .header("Content-Disposition", 
                    "attachment; filename=\"" + document.getDocumentName() + "\"")
                .header("Content-Length", fileContent.length)
                .build();
            
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement du document {}", id, e);
            
            ApiResponse<Object> errorResponse = new ApiResponse<>();
            errorResponse.setStatus_code(500);
            errorResponse.setStatus_message("Erreur lors du téléchargement du document: " + e.getMessage());
            
            return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
        }
    }
    
    /**
     * Récupère les informations d'un document (sans le contenu binaire)
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Obtenir les informations d'un document",
               description = "Retourne les métadonnées d'un document par son ID")
    public Response getDocumentInfo(@PathParam("id") Long id) {
        ApiResponse<DocumentEntity> response = new ApiResponse<>();
        
        try {
            DocumentEntity document = documentRepository.findById(id);
            
            if (document == null) {
                response.setStatus_code(404);
                response.setStatus_message("Document non trouvé");
                response.setData(null);
                return Response.status(404).entity(response).build();
            }
            
            response.setStatus_code(7000);
            response.setStatus_message("Success");
            response.setData(document);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du document {}", id, e);
            response.setStatus_code(500);
            response.setStatus_message("Erreur lors de la récupération du document");
            response.setData(null);
            return Response.status(500).entity(response).build();
        }
    }
}

