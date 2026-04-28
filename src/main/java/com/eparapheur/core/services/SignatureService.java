package com.eparapheur.core.services;

import com.eparapheur.db.entities.*;
import com.eparapheur.db.repositories.SignatureActionRepository;
import com.eparapheur.db.repositories.SignedDocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Base64;

@ApplicationScoped
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    @Inject
    FileStorageService fileStorageService;

    @Inject
    SignatureActionRepository signatureActionRepository;

    @Inject
    SignedDocumentRepository signedDocumentRepository;



    /**
     * Calcule le hash SHA-256 d'un fichier.
     */
    public String calculateDocumentHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Applique un visuel de signature sur un PDF.
     * Pour l'instant, cette méthode génère un nouveau fichier PDF avec l'image incrustée.
     */
    public String applySignatureVisual(DocumentEntity document, UserSignatureVisualEntity visual, SignatureActionEntity action) throws Exception {
        //File inputFile = new File(document.getDocumentPath());



        Path inputPath = Paths.get(
                "uploads",
                "documents",
                document.getDocumentPath()
        );

        File inputFile = inputPath.toFile();

        logger.info("Document path DB: {}", document.getDocumentPath());
        logger.info("Absolute path: {}", inputFile.getAbsolutePath());
        logger.info("Exists: {}", inputFile.exists());


        if (!inputFile.exists()) {
            throw new Exception("Fichier source introuvable : " + document.getDocumentPath());
        }

        try (PDDocument pdf = Loader.loadPDF(inputFile)) {
            int pageNum = (action.getSignaturePage() != null) ? action.getSignaturePage() - 1 : 0;
            if (pageNum < 0 || pageNum >= pdf.getNumberOfPages()) {
                pageNum = 0;
            }

            PDPage page = pdf.getPage(pageNum);
            
            // Chargement de l'image de signature
            Path visualPath = fileStorageService.getAbsolutePath(
                    visual.getVisualPath()
            );

            File visualFile = visualPath.toFile();

            if (!visualFile.exists()) {
                throw new Exception("Visuel introuvable");
            }

            if (visualFile.exists()) {
                PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(visualFile, pdf);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    float x = action.getSignatureX() != null ? action.getSignatureX().floatValue() : 100;
                    float y = action.getSignatureY() != null ? action.getSignatureY().floatValue() : 100;
                    float width = action.getSignatureWidth() != null ? action.getSignatureWidth().floatValue() : 150;
                    float height = action.getSignatureHeight() != null ? action.getSignatureHeight().floatValue() : 50;
                    
                    contentStream.drawImage(pdImage, x, y, width, height);
                }
            }

            // Sauvegarde du fichier intermédiaire/final
            String outputFileName = "signed_" + System.currentTimeMillis() + "_" + document.getDocumentName();
            File tempFile = Files.createTempFile("sign_", ".pdf").toFile();
            pdf.save(tempFile);
            
            // Stockage définitif
            return fileStorageService.saveBase64File(outputFileName, Base64.getEncoder().encodeToString(Files.readAllBytes(tempFile.toPath())));
        }
    }

    /**
     * Enregistre l'acte de signature final.
     */
    @Transactional
    public void recordSignatureAction(SignatureActionEntity action) {
        signatureActionRepository.persist(action);
    }
}
