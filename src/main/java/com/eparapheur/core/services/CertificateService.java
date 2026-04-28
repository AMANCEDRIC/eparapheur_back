package com.eparapheur.core.services;

import com.eparapheur.db.entities.UserCertificateEntity;
import com.eparapheur.db.repositories.UserCertificateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Base64;

@ApplicationScoped
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Inject
    UserCertificateRepository certificateRepository;

    /**
     * Parse un certificat au format PEM et extrait ses métadonnées dans une entité.
     */
    public UserCertificateEntity parseCertificate(String pemContent, Long accountId) throws Exception {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemContent.getBytes())))) {
            Object object = pemParser.readObject();
            if (!(object instanceof X509CertificateHolder)) {
                throw new IllegalArgumentException("Le contenu fourni n'est pas un certificat X509 valide.");
            }

            X509CertificateHolder holder = (X509CertificateHolder) object;
            X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

            UserCertificateEntity entity = new UserCertificateEntity();
            entity.setIdAccount(accountId);
            entity.setCertificatePem(pemContent);
            entity.setSerialNumber(cert.getSerialNumber().toString());
            entity.setIssuer(cert.getIssuerX500Principal().getName());
            entity.setSubject(cert.getSubjectX500Principal().getName());
            entity.setValidFrom(new Timestamp(cert.getNotBefore().getTime()));
            entity.setValidUntil(new Timestamp(cert.getNotAfter().getTime()));
            
            // Empreinte SHA-256
            entity.setPublicKeyFingerprint(calculateFingerprint(cert.getEncoded()));
            
            // Valeurs par défaut pour les nouveaux certificats
            entity.setGenerationState("active");
            entity.setCertificateType("ca_issued"); // Par défaut, à affiner selon le besoin
            entity.setSignatureLevel("simple");
            entity.setActive(true);

            return entity;
        }
    }

    /**
     * Calcule l'empreinte SHA-256 d'un tableau d'octets.
     */
    private String calculateFingerprint(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Enregistre un certificat en base de données.
     */
    @Transactional
    public void saveCertificate(UserCertificateEntity entity) {
        certificateRepository.persist(entity);
    }
}
