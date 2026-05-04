package com.eparapheur.core.services;

import com.eparapheur.db.entities.AccountEntity;
import com.eparapheur.db.entities.UserCertificateEntity;
import com.eparapheur.db.entities.UserPrivateKeyEntity;
import com.eparapheur.db.repositories.UserPrivateKeyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.bouncycastle.util.encoders.Base64;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ApplicationScoped
public class CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);

    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @ConfigProperty(name = "crypto.master-key", defaultValue = "default-master-key-must-change-in-prod-123")
    String masterKey;

    @Inject
    UserPrivateKeyRepository privateKeyRepository;

    @Inject
    CertificateService certificateService;

    @Inject
    com.eparapheur.db.repositories.UserCertificateRepository certificateRepository;

    /**
     * Génère une identité complète pour l'utilisateur (Clés RSA + Certificat X.509).
     */
    @Transactional
    public void generateUserIdentity(AccountEntity account) throws Exception {
        // 1. Génération RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        
        byte[] privateKeyBytes = pair.getPrivate().getEncoded();
        byte[] publicKeyBytes = pair.getPublic().getEncoded();
        String publicKeyBase64 = Base64.toBase64String(publicKeyBytes);

        // 2. Chiffrement de la clé privée
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);
        
        byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);

        // 3. Stockage de la clé privée
        UserPrivateKeyEntity keyEntity = new UserPrivateKeyEntity();
        keyEntity.setIdAccount(account.getId());
        keyEntity.setPrivateKeyEncrypted(Base64.toBase64String(encryptedPrivateKey));
        keyEntity.setEncryptionIv(Base64.toBase64String(iv));
        keyEntity.setKeyAlgorithm("RSA");
        keyEntity.setKeySize(2048);
        keyEntity.setActive(true);
        privateKeyRepository.persist(keyEntity);

        // 4. Génération du certificat auto-signé via CertificateService
        UserCertificateEntity certEntity = certificateService.generateSelfSignedCertificate(account, publicKeyBase64, pair.getPrivate());
        
        // 5. Lier la clé au certificat
        keyEntity.setIdCertificate(certEntity.getId());
        privateKeyRepository.persist(keyEntity);
        
        logger.info("Identité numérique générée avec succès pour l'utilisateur id={}", account.getId());
    }

    /**
     * Signe un hash avec la clé privée de l'utilisateur.
     */
    public String signHash(String documentHashHex, Long accountId) throws Exception {
        PrivateKey privateKey = getPrivateKey(accountId);

        // Signer
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(hexStringToByteArray(documentHashHex));
        
        byte[] signatureBytes = signature.sign();
        return Base64.toBase64String(signatureBytes);
    }

    /**
     * Récupère et déchiffre la clé privée d'un utilisateur.
     */
    public PrivateKey getPrivateKey(Long accountId) throws Exception {
        UserPrivateKeyEntity entity = privateKeyRepository.find("idAccount", accountId).firstResult();
        if (entity == null) {
            throw new Exception("Clé privée introuvable pour ce compte.");
        }

        byte[] iv = Base64.decode(entity.getEncryptionIv());
        byte[] encryptedKey = Base64.decode(entity.getPrivateKeyEncrypted());

        Cipher cipher = Cipher.getInstance(AES_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);
        
        byte[] privateKeyBytes = cipher.doFinal(encryptedKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public UserPrivateKeyEntity getPrivateKeyEntity(Long accountId) {
        return privateKeyRepository.find("idAccount", accountId).firstResult();
    }

    /**
     * Récupère le certificat X.509 d'un utilisateur.
     */
    public java.security.cert.X509Certificate getX509Certificate(Long accountId) throws Exception {
        UserCertificateEntity entity = certificateRepository.find("idAccount = ?1 AND active = true", accountId).firstResult();
        if (entity == null) {
            throw new Exception("Certificat introuvable pour ce compte.");
        }

        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        return (java.security.cert.X509Certificate) cf.generateCertificate(
                new java.io.ByteArrayInputStream(entity.getCertificatePem().getBytes(StandardCharsets.UTF_8))
        );
    }

    /**
     * Vérifie une signature.
     */
    public boolean verifySignature(String documentHashHex, String signatureBase64, String publicKeyBase64) throws Exception {
        byte[] publicKeyBytes = Base64.decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(hexStringToByteArray(documentHashHex));

        return signature.verify(Base64.decode(signatureBase64));
    }

    private SecretKey getSecretKey() {
        // On s'assure que la clé fait 32 octets (256 bits)
        byte[] keyBytes = new byte[32];
        byte[] masterBytes = masterKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(masterBytes, 0, keyBytes, 0, Math.min(masterBytes.length, 32));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
