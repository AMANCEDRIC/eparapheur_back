package com.eparapheur.resources;

import com.eparapheur.core.services.FileStorageService;
import com.eparapheur.db.entities.DocumentEntity;
import com.eparapheur.db.entities.SignedDocumentEntity;
import com.eparapheur.db.repositories.DocumentRepository;
import com.eparapheur.db.repositories.SignedDocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Path("/files")
@ApplicationScoped
public class FileResource {

    private static final Logger logger = LoggerFactory.getLogger(FileResource.class);

    @Inject
    FileStorageService fileStorageService;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    SignedDocumentRepository signedDocumentRepository;

    /**
     * Sert les visuels de signature.
     * URL: /api/files/visuals/2026/04/28/sig_xxx.png
     */
    @GET
    @Path("/visuals/{path:.+}")
    public Response getVisual(@PathParam("path") String path) {
        return serveFile("visuals/" + path);
    }

    /**
     * Sert un document (original ou signé).
     * URL: /api/files/documents/{id}
     */
    @GET
    @Path("/documents/{id}")
    public Response getDocument(@PathParam("id") Long id) {
        // 1. Chercher si une version signée existe
        SignedDocumentEntity signedDoc = signedDocumentRepository.find("idDocument", id).firstResult();
        
        String relativePath;
        String fileName;
        String contentType = "application/pdf";

        if (signedDoc != null) {
            relativePath = signedDoc.getSignedPath();
            fileName = "signed_" + signedDoc.getDocument().getDocumentName();
        } else {
            // 2. Sinon, prendre l'original
            DocumentEntity doc = documentRepository.findById(id);
            if (doc == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            relativePath = doc.getDocumentPath();
            fileName = doc.getDocumentName();
            contentType = doc.getDocumentType() != null ? doc.getDocumentType() : "application/pdf";
        }

        return serveFile(relativePath, fileName, contentType);
    }

    private Response serveFile(String relativePath) {
        return serveFile(relativePath, null, null);
    }

    private Response serveFile(String relativePath, String downloadName, String contentType) {
        java.nio.file.Path absolutePath = fileStorageService.getAbsolutePath(relativePath);
        File file = absolutePath.toFile();

        if (!file.exists()) {
            logger.warn("Fichier introuvable: {}", file.getAbsolutePath());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Response.ResponseBuilder response = Response.ok(file);
        
        if (contentType != null) {
            response.type(contentType);
        } else {
            // Déduction simple
            if (relativePath.endsWith(".png")) response.type("image/png");
            else if (relativePath.endsWith(".jpg") || relativePath.endsWith(".jpeg")) response.type("image/jpeg");
            else if (relativePath.endsWith(".pdf")) response.type("application/pdf");
        }

        if (downloadName != null) {
            response.header("Content-Disposition", "inline; filename=\"" + downloadName + "\"");
        }

        return response.build();
    }
}
