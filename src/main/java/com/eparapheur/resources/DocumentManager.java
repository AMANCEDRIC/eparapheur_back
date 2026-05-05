package com.eparapheur.resources;

import com.eparapheur.core.features.ApiResponse;
import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.db.entities.DocumentEntity;
import com.eparapheur.db.entities.SignedDocumentEntity;
import com.eparapheur.db.repositories.DocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.ByteArrayInputStream;

@Path("/documents")
@ApplicationScoped
public class DocumentManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);
    
    @Inject
    DocumentRepository documentRepository;
    
    @Inject
    FileStorageService fileStorageService;

    @Inject
    com.eparapheur.db.repositories.SignedDocumentRepository signedDocumentRepository;

    @Inject
    com.eparapheur.db.repositories.StepParticipantRepository participantRepository;

    @Inject
    com.eparapheur.db.repositories.ProgramStepRepository stepRepository;
    
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

            // 1b. Vérifier si le document est lié à un programme et si celui-ci est terminé
            SignedDocumentEntity signedDoc = signedDocumentRepository.find("idDocument", id).firstResult();
            
            String pathToServe = document.getDocumentPath();
            String fileNameToServe = document.getDocumentName();

            if (signedDoc != null) {
                Long programId = signedDoc.getIdProgram();
                // Vérifier si tous les participants du programme ont signé
                long totalParticipants = participantRepository.count("step.idProgram = ?1", programId);
                long completedParticipants = participantRepository.count("step.idProgram = ?1 AND status = 'COMPLETED'", programId);
                
                if (completedParticipants < totalParticipants) {
                    ApiResponse<Object> forbiddenResponse = new ApiResponse<>();
                    forbiddenResponse.setStatus_code(403);
                    forbiddenResponse.setStatus_message("Le document ne peut pas être téléchargé tant que tous les signataires n'ont pas signé.");
                    return Response.status(403)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(forbiddenResponse)
                        .build();
                }

                // Si terminé, on sert la version signée
                pathToServe = signedDoc.getSignedPath();
                fileNameToServe = "signed_" + fileNameToServe;
            }
            
            // 2. Récupérer le contenu du fichier depuis le stockage
            byte[] fileContent = fileStorageService.getFileContent(pathToServe);
            
            // 3. Déterminer le Content-Type
            String contentType = document.getDocumentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            
            // 4. Retourner le fichier avec les headers appropriés
            return Response.ok(new ByteArrayInputStream(fileContent))
                .type(contentType)
                .header("Content-Disposition", 
                    "attachment; filename=\"" + fileNameToServe + "\"")
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

    /**
     * Télécharge le dossier de preuve ZIP pour un programme
     */
    @GET
    @Path("/proof/{programId}")
    @Produces("application/zip")
    @Operation(summary = "Télécharger le dossier de preuve",
               description = "Retourne le fichier ZIP contenant les preuves du programme")
    public Response downloadProof(@PathParam("programId") Long programId) {
        com.eparapheur.db.entities.SignatureProgramEntity program = com.eparapheur.db.entities.SignatureProgramEntity.findById(programId);
        if (program == null || program.getProofPath() == null) {
            return Response.status(404).build();
        }

        java.nio.file.Path path = fileStorageService.getAbsolutePath(program.getProofPath());
        File file = path.toFile();

        if (!file.exists()) {
            return Response.status(404).build();
        }

        String fileName = "Dossier_de_preuve_" + program.getTitle().replaceAll("\\s+", "_") + ".zip";
        return Response.ok(file)
            .type("application/zip")
            .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
            .build();
    }
}

