package com.eparapheur.core.services;

import com.eparapheur.db.entities.AccountEntity;
import com.eparapheur.db.entities.PersonEntity;
import com.eparapheur.db.entities.UserCertificateEntity;
import com.eparapheur.db.repositories.PersonRepository;
import com.eparapheur.db.repositories.UserCertificateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

@ApplicationScoped
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Inject
    UserCertificateRepository certificateRepository;

    @Inject
    PersonRepository personRepository;

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
            entity.setCertificateType("ca_issued");
            entity.setSignatureLevel("simple");
            entity.setActive(true);

            return entity;
        }
    }

    /**
     * Génère un certificat auto-signé pour un utilisateur.
     */
    @Transactional
    public UserCertificateEntity generateSelfSignedCertificate(AccountEntity account, String publicKeyBase64, PrivateKey privateKey) throws Exception {
        PersonEntity person = personRepository.findById(account.getIdUser());
        String commonName = (person != null) ? person.getPrenUser() + " " + person.getNomUser() : account.getLoginCmpt();
        
        // 1. Reconstruire la clé publique
        byte[] pubKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(pubKeySpec);

        // 2. Création du sujet
        X500Name subject = new X500Name("CN=" + commonName + ", E=" + account.getLoginCmpt() + ", O=E-Parapheur, C=FR");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + (365L * 24 * 60 * 60 * 1000)); // 1 an

        // 3. Builder le certificat
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, publicKey);
        
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        X509CertificateHolder holder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

        // 4. Exporter en PEM
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            pemWriter.writeObject(cert);
        }
        String pem = sw.toString();

        // 5. Créer l'entité
        UserCertificateEntity entity = new UserCertificateEntity();
        entity.setIdAccount(account.getId());
        entity.setCertificatePem(pem);
        entity.setSerialNumber(cert.getSerialNumber().toString());
        entity.setIssuer(cert.getIssuerX500Principal().getName());
        entity.setSubject(cert.getSubjectX500Principal().getName());
        entity.setValidFrom(new Timestamp(cert.getNotBefore().getTime()));
        entity.setValidUntil(new Timestamp(cert.getNotAfter().getTime()));
        entity.setPublicKeyFingerprint(calculateFingerprint(cert.getEncoded()));
        entity.setGenerationState("active");
        entity.setCertificateType("self_signed");
        entity.setSignatureLevel("avancee");
        entity.setActive(true);

        certificateRepository.persist(entity);
        return entity;
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
