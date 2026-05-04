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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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
     * Signe numériquement un PDF (PAdES) avec un visuel optionnel.
     */
    public String signDocumentPAdES(String relativeInputPath, String originalFileName, UserSignatureVisualEntity visual, SignatureActionEntity action, PrivateKey privateKey, X509Certificate certificate) throws Exception {
        Path inputPath = fileStorageService.getAbsolutePath(relativeInputPath);
        File inputFile = inputPath.toFile();

        if (!inputFile.exists()) {
            throw new Exception("Fichier source introuvable : " + relativeInputPath);
        }

        // Fichier temporaire pour le résultat
        String outputFileName = "signed_" + System.currentTimeMillis() + "_" + originalFileName;
        File tempFile = Files.createTempFile("sign_pades_", ".pdf").toFile();

        try (PDDocument doc = Loader.loadPDF(inputFile)) {
            // 1. Préparation de la signature
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
            
            String signerName = certificate.getSubjectX500Principal().getName().split(",")[0].replace("CN=", "");
            signature.setName(signerName);
            signature.setLocation("Côte d'Ivoire");
            signature.setReason("Signature électronique de " + signerName);
            signature.setSignDate(Calendar.getInstance());

            // 2. Options de signature (Visuel)
            SignatureOptions options = new SignatureOptions();
            if (visual != null) {
                int pageNum = (action.getSignaturePage() != null) ? action.getSignaturePage() - 1 : 0;
                options.setPage(pageNum);
                
                // Positionnement rectangulaire pour le widget de signature
                float x = action.getSignatureX() != null ? action.getSignatureX().floatValue() : 100;
                float y = action.getSignatureY() != null ? action.getSignatureY().floatValue() : 100;
                float width = action.getSignatureWidth() != null ? action.getSignatureWidth().floatValue() : 150;
                float height = action.getSignatureHeight() != null ? action.getSignatureHeight().floatValue() : 50;
                
                PDRectangle rect = new PDRectangle(x, y, width, height);
                options.setVisualSignature(createVisualSignature(doc, pageNum, rect, visual, signerName));
            }

            // 3. Ajout de la signature au document
            doc.addSignature(signature, new CmsSignatureInterface(privateKey, certificate), options);

            // 4. Sauvegarde incrémentale
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                doc.saveIncremental(fos);
            }
        }

        // 5. Stockage définitif
        String savedPath = fileStorageService.saveBase64File(outputFileName, 
                Base64.getEncoder().encodeToString(Files.readAllBytes(tempFile.toPath())), 
                FileStorageService.StorageType.SIGNED);
        
        tempFile.delete();
        return savedPath;
    }

    /**
     * Crée l'apparence visuelle de la signature numérique.
     */
    private InputStream createVisualSignature(PDDocument doc, int pageNum, PDRectangle rect, UserSignatureVisualEntity visual, String signerName) throws Exception {
        Path visualPath = fileStorageService.getAbsolutePath(visual.getVisualPath());
        try (FileInputStream fis = new FileInputStream(visualPath.toFile())) {
            PDVisibleSignDesigner designer = new PDVisibleSignDesigner(doc, fis, pageNum + 1);
            designer.xAxis(rect.getLowerLeftX()).yAxis(rect.getLowerLeftY()).width(rect.getWidth()).height(rect.getHeight());
            
            PDVisibleSigProperties properties = new PDVisibleSigProperties();
            properties.signerName(signerName)
                      .signerLocation("Côte d'Ivoire")
                      .signatureReason("Signature Électronique")
                      .preferredSize(0)
                      .page(pageNum + 1)
                      .visualSignEnabled(true)
                      .setPdVisibleSignature(designer);
            
            properties.buildSignature();
            return properties.getVisibleSignature();
        }
    }

    @Transactional
    public void recordSignatureAction(SignatureActionEntity action) {
        signatureActionRepository.persist(action);
    }

    /**
     * Implémentation interne de la signature CMS/PKCS7.
     */
    private static class CmsSignatureInterface implements SignatureInterface {
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        public CmsSignatureInterface(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        @Override
        public byte[] sign(InputStream content) throws IOException {
            try {
                List<X509Certificate> certList = new ArrayList<>();
                certList.add(certificate);
                Store certs = new JcaCertStore(certList);
                
                CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
                ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
                
                gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build())
                        .build(sha256Signer, certificate));
                gen.addCertificates(certs);
                
                CMSProcessableByteArray msg = new CMSProcessableByteArray(content.readAllBytes());
                CMSSignedData signedData = gen.generate(msg, false);
                
                return signedData.getEncoded();
            } catch (Exception e) {
                throw new IOException("Erreur lors de la génération de la signature CMS", e);
            }
        }
    }
}
