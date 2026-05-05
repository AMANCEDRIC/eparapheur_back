package com.eparapheur.core.services;

import com.eparapheur.db.entities.*;
import com.eparapheur.db.repositories.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class ProofService {

    private static final Logger logger = LoggerFactory.getLogger(ProofService.class);

    @Inject
    SignatureProgramRepository programRepository;

    @Inject
    SignatureActionRepository actionRepository;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    SignedDocumentRepository signedDocumentRepository;

    @Inject
    StepParticipantRepository participantRepository;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    PersonRepository personRepository;

    /**
     * Génère un dossier de preuve (ZIP) pour un programme de signature terminé.
     */
    public String generateProofFolder(Long programId) throws Exception {
        SignatureProgramEntity program = programRepository.findById(programId);
        if (program == null) throw new Exception("Programme introuvable.");

        logger.info("Génération du dossier de preuve pour le programme: {}", program.getTitle());

        // 1. Créer un fichier ZIP temporaire
        File tempZip = File.createTempFile("proof_" + programId + "_", ".zip");
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            
            // 2. Ajouter les documents (originaux et signés)
            java.util.Set<Long> processedDocIds = new java.util.HashSet<>();
            for (ProgramStepEntity step : program.getSteps()) {
                for (DocumentEntity doc : step.getDocuments()) {
                    if (processedDocIds.contains(doc.getId())) continue;
                    processedDocIds.add(doc.getId());

                    SignedDocumentEntity signedDoc = signedDocumentRepository.find("idDocument", doc.getId()).firstResult();
                    if (signedDoc != null) {
                        addFileToZip(zos, "documents_signes/" + doc.getDocumentName(), signedDoc.getSignedPath());
                    }
                    // Ajouter l'original
                    addFileToZip(zos, "documents_originaux/" + doc.getDocumentName(), doc.getDocumentPath());
                }
            }

            // 3. Générer et ajouter le Journal de Preuve (Manifest)
            byte[] manifestPdf = generateManifestPdf(program);
            ZipEntry manifestEntry = new ZipEntry("Journal_de_Preuve.pdf");
            zos.putNextEntry(manifestEntry);
            zos.write(manifestPdf);
            zos.closeEntry();
        }

        // 4. Sauvegarder le ZIP via FileStorageService
        String zipName = "preuve_" + programId + "_" + System.currentTimeMillis() + ".zip";
        byte[] zipBytes = Files.readAllBytes(tempZip.toPath());
        String savedPath = fileStorageService.saveBase64File(zipName, 
                Base64.getEncoder().encodeToString(zipBytes), 
                FileStorageService.StorageType.PROOFS);
        
        tempZip.delete();
        
        // Sauvegarder le chemin dans le programme
        program.setProofPath(savedPath);
        programRepository.persist(program);

        logger.info("Dossier de preuve généré et associé au programme: {}", savedPath);
        return savedPath;
    }

    private void addFileToZip(ZipOutputStream zos, String entryName, String relativePath) throws IOException {
        Path path = fileStorageService.getAbsolutePath(relativePath);
        if (Files.exists(path)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            Files.copy(path, zos);
            zos.closeEntry();
        }
    }

    private byte[] generateManifestPdf(SignatureProgramEntity program) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                content.newLineAtOffset(50, 780);
                content.showText("JOURNAL DE PREUVE DE SIGNATURE");
                
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(0, -30);
                content.showText("Programme : " + program.getTitle());
                content.newLineAtOffset(0, -20);
                content.showText("ID Programme : " + program.getId());
                content.newLineAtOffset(0, -20);
                content.showText("Date de génération : " + sdf.format(new java.util.Date()));
                
                content.newLineAtOffset(0, -40);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.showText("Historique des signatures :");
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                content.newLineAtOffset(0, -25);

                // Récupérer les actions de signature pour ce programme
                // Note: On passe par le champ 'participant' qui a une relation avec 'step'
                List<SignatureActionEntity> actions = actionRepository.find("participant.step.program.id", program.getId()).list();
                
                for (SignatureActionEntity action : actions) {
                    StepParticipantEntity p = action.getParticipant();
                    String name = "Inconnu";
                    if (p != null && p.getAccount() != null) {
                        PersonEntity person = personRepository.findById(p.getAccount().getIdUser());
                        if (person != null) {
                            name = person.getPrenUser() + " " + person.getNomUser();
                        }
                    }
                    
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                    content.showText("- " + name);
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    content.newLineAtOffset(0, -12);
                    content.showText("  IP: " + action.getIpAddress() + " | Date: " + sdf.format(action.getSignedAt()));
                    content.newLineAtOffset(0, -12);
                    content.showText("  Type: " + action.getActionType() + " | Niveau: " + action.getSignatureLevel());
                    content.newLineAtOffset(0, -12);
                    content.showText("  Hash Document (avant): " + action.getDocumentHashBefore().substring(0, 32) + "...");
                    content.newLineAtOffset(0, -18);
                    
                    // Gestion simple de la pagination si trop d'actions (simplifié ici)
                }
                
                content.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }
}
