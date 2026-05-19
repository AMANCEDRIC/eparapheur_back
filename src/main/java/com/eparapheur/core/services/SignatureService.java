package com.eparapheur.core.services;

import com.eparapheur.db.entities.*;
import com.eparapheur.db.repositories.SignatureActionRepository;
import com.eparapheur.db.repositories.SignedDocumentRepository;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

@ApplicationScoped
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    @Inject
    FileStorageService fileStorageService;

    @Inject
    SignatureActionRepository signatureActionRepository;

    @Inject
    SignedDocumentRepository signedDocumentRepository;

    @Inject
    CryptoService cryptoService;

    @Inject
    DssConfigService dssConfigService;

    // -------------------------------------------------------------------------
    // Hash utilitaire (conservé pour l'audit trail)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Signature PAdES via DSS (PAdES-BASELINE-T avec horodatage TSA)
    // -------------------------------------------------------------------------

    /**
     * Signe numériquement un PDF au niveau PAdES-BASELINE-T via la librairie DSS
     * de l'Union Européenne. Inclut un horodatage RFC 3161 via FreeTSA.
     *
     * @param relativeInputPath Chemin relatif du fichier source (original ou déjà signé)
     * @param originalFileName  Nom original du fichier (pour nommer le résultat)
     * @param visual            Entité visuel de signature (peut être null)
     * @param action            Acte de signature (coordonnées, page, etc.)
     * @param privateKey        Clé privée RSA du signataire (déchiffrée)
     * @param certificate       Certificat X.509 du signataire
     * @param displayIdentity   Si true, affiche nom + date dans le visuel
     * @return Chemin relatif du fichier signé sauvegardé
     */
    public String signDocumentPAdES(
            String relativeInputPath,
            String originalFileName,
            UserSignatureVisualEntity visual,
            SignatureActionEntity action,
            PrivateKey privateKey,
            X509Certificate certificate,
            boolean displayIdentity) throws Exception {

        Path inputPath = fileStorageService.getAbsolutePath(relativeInputPath);
        File inputFile = inputPath.toFile();

        if (!inputFile.exists()) {
            throw new Exception("Fichier source introuvable : " + relativeInputPath);
        }

        logger.info("Démarrage signature PAdES-BASELINE-T pour : {}", originalFileName);

        // ─── 1. Charger le document source en mémoire (format DSS) ─────────
        byte[] inputBytes = Files.readAllBytes(inputFile.toPath());
        DSSDocument toSignDocument = new InMemoryDocument(inputBytes, originalFileName, MimeTypeEnum.PDF);

        // ─── 2. Construire les paramètres de signature PAdES ────────────────
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        // Niveau T = Baseline + Timestamp TSA (RFC 3161)
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

        // Identité du signataire
        CertificateToken certToken = new CertificateToken(certificate);
        parameters.setSigningCertificate(certToken);
        parameters.setCertificateChain(List.of(certToken));

        // Date de signature
        parameters.bLevel().setSigningDate(Calendar.getInstance().getTime());

        // Nom du signataire (extrait du CN du certificat)
        String subjectName = certificate.getSubjectX500Principal().getName();
        String signerName = subjectName;
        if (subjectName.contains("CN=")) {
            signerName = subjectName.substring(subjectName.indexOf("CN=") + 3).split(",")[0];
        }

        // ─── 3. Paramètres visuels (si un visuel est fourni) ────────────────
        if (visual != null) {
            SignatureImageParameters imageParameters = buildSignatureImageParameters(
                    inputFile, visual, action, signerName, displayIdentity);
            parameters.setImageParameters(imageParameters);
        }

        // ─── 4. Obtenir les données à signer (ToBeSigned) ───────────────────
        PAdESService padesService = dssConfigService.buildPadesService();
        ToBeSigned dataToSign = padesService.getDataToSign(toSignDocument, parameters);

        // ─── 5. Signer avec la clé privée de l'utilisateur (JCA standard) ───
        java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(dataToSign.getBytes());
        byte[] rawSignatureBytes = signer.sign();

        SignatureValue signatureValue = new SignatureValue(
                SignatureAlgorithm.RSA_SHA256,
                rawSignatureBytes
        );

        // ─── 6. Finaliser la signature (DSS applique le timestamp TSA ici) ──
        DSSDocument signedDocument = padesService.signDocument(toSignDocument, parameters, signatureValue);

        // ─── 7. Sauvegarder le document signé ───────────────────────────────
        String outputFileName = "signed_" + System.currentTimeMillis() + "_" + originalFileName;
        byte[] signedBytes = toByteArray(signedDocument);

        String savedPath = fileStorageService.saveBase64File(
                outputFileName,
                Base64.getEncoder().encodeToString(signedBytes),
                FileStorageService.StorageType.SIGNED
        );

        logger.info("Signature PAdES-BASELINE-T terminée → {}", savedPath);
        return savedPath;
    }

    // -------------------------------------------------------------------------
    // Construction du visuel de signature (conservé et adapté pour DSS)
    // -------------------------------------------------------------------------

    /**
     * Construit les paramètres d'apparence visuelle pour DSS.
     * Génère une image composite (visuel utilisateur + texte d'identité si demandé)
     * puis la passe à DSS via SignatureImageParameters.
     */
    private SignatureImageParameters buildSignatureImageParameters(
            File inputFile,
            UserSignatureVisualEntity visual,
            SignatureActionEntity action,
            String signerName,
            boolean displayIdentity) throws Exception {

        Path visualPath = fileStorageService.getAbsolutePath(visual.getVisualPath());

        // Coordonnées du rectangle de signature
        float x      = action.getSignatureX()      != null ? action.getSignatureX().floatValue()     : 100f;
        float y      = action.getSignatureY()       != null ? action.getSignatureY().floatValue()      : 100f;
        float width  = action.getSignatureWidth()   != null ? action.getSignatureWidth().floatValue() : 150f;
        float height = action.getSignatureHeight()  != null ? action.getSignatureHeight().floatValue(): 50f;
        int   page   = action.getSignaturePage()    != null ? action.getSignaturePage()              : 1;

        // Déterminer la hauteur de la page PDF pour ajuster l'axe Y (le frontend envoie des coordonnées standard PDF orientées bas-haut)
        float pageHeight = 842f; // Valeur par défaut A4
        try {
            try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(inputFile)) {
                int pdfBoxPageIndex = page - 1;
                if (pdfBoxPageIndex >= 0 && pdfBoxPageIndex < doc.getNumberOfPages()) {
                    org.apache.pdfbox.pdmodel.PDPage pdPage = doc.getPage(pdfBoxPageIndex);
                    pageHeight = pdPage.getMediaBox().getHeight();
                    logger.debug("Hauteur réelle de la page {} lue via PDFBox : {} points", page, pageHeight);
                }
            }
        } catch (Exception e) {
            logger.warn("Impossible de lire la hauteur de la page via PDFBox, utilisation de 842 : {}", e.getMessage());
        }

        // Conversion de l'origine Y (bas-haut en haut-bas pour DSS)
        float adjustedY = pageHeight - y - height;

        // Générer l'image du visuel (avec ou sans identité textuelle)
        byte[] imageBytes = buildVisualImage(visualPath, signerName, displayIdentity);

        // Configurer les paramètres DSS pour l'image
        SignatureImageParameters imageParams = new SignatureImageParameters();

        SignatureFieldParameters fieldParams = new SignatureFieldParameters();
        fieldParams.setOriginX(x);
        fieldParams.setOriginY(adjustedY);
        fieldParams.setWidth(width);
        fieldParams.setHeight(height);
        fieldParams.setPage(page);
        imageParams.setFieldParameters(fieldParams);

        // Image composite comme fond du widget de signature
        imageParams.setImage(new InMemoryDocument(imageBytes));

        return imageParams;
    }

    /**
     * Génère l'image composite du visuel de signature.
     * - Sans displayIdentity : image brute du visuel utilisateur
     * - Avec displayIdentity : image (70%) + "Signé par X le JJ/MM/AAAA" (30%)
     */
    private byte[] buildVisualImage(Path visualPath, String signerName, boolean displayIdentity) throws Exception {
        if (!displayIdentity) {
            return Files.readAllBytes(visualPath);
        }

        // Image combinée : visuel + texte d'identité
        BufferedImage baseImage = ImageIO.read(visualPath.toFile());
        if (baseImage == null) {
            throw new IOException("Impossible de lire l'image de signature : " + visualPath);
        }

        int width  = 600;
        int height = 300;
        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();

        // Rendu haute qualité
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,      RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Zone image (70%)
        int imageHeight = (int) (height * 0.7);
        g.drawImage(baseImage, 0, 0, width, imageHeight, null);

        // Zone texte (30%)
        g.setColor(Color.BLACK);
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());
        g.setFont(new Font("Serif", Font.ITALIC, 40));
        g.drawString("Signé par " + signerName, 20, imageHeight + 45);
        g.setFont(new Font("Serif", Font.PLAIN, 34));
        g.drawString("le " + dateStr, 20, imageHeight + 90);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combined, "PNG", baos);
        return baos.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Persistance de l'acte de signature
    // -------------------------------------------------------------------------

    @Transactional
    public void recordSignatureAction(SignatureActionEntity action) {
        signatureActionRepository.persist(action);
    }

    // -------------------------------------------------------------------------
    // Utilitaire interne
    // -------------------------------------------------------------------------

    private byte[] toByteArray(DSSDocument doc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.writeTo(baos);
        return baos.toByteArray();
    }
}
