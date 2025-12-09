package com.eparapheur.core.services;

import com.eparapheur.db.entities.OtpEntity;
import com.eparapheur.db.repositories.OtpRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_VALIDITY_MINUTES = 10;

    @Inject
    OtpRepository otpRepository;

    /**
     * Génère un code OTP aléatoire à 6 chiffres
     * @return Code OTP (ex: "123456")
     */
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Entre 100000 et 999999
        return String.valueOf(otp);
    }

    /**
     * Crée et sauvegarde un OTP en base de données
     * @param accountId ID du compte demandeur
     * @param otpCode Code OTP généré
     * @param action Type d'action (TWO_FACTOR_AUTH, CREAT_PROGRAM)
     * @param canal Canal d'envoi (EMAIL, SMS, WHATSAPP)
     * @return L'entité OTP créée
     */
    @Transactional
    public OtpEntity createOtp(Long accountId, String otpCode, String action, String canal) {
        OtpEntity otp = new OtpEntity();
        otp.setIdDemandingAccount(accountId);
        otp.setOtp(otpCode);
        otp.setAction(action);
        otp.setCanal(canal);
        
        // Date de création
        Timestamp now = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        otp.setCreatedAt(now);
        
        // Date d'expiration (10 minutes)
        Timestamp validityDate = Timestamp.from(
            LocalDateTime.now()
                .plusMinutes(OTP_VALIDITY_MINUTES)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
        otp.setValidityDate(validityDate);
        
        // Valeurs par défaut
        otp.setIsActive((byte) 1);
        otp.setIsDeleted((byte) 0);
        otp.setErrorSendOtp((byte) 0);
        otp.setUsedDate(null);
        otp.setDeletedAt(null);
        
        otpRepository.persist(otp);
        logger.info("OTP créé pour accountId: {}, action: {}, canal: {}", accountId, action, canal);
        
        return otp;
    }

    /**
     * Vérifie si un OTP est valide (non expiré, non utilisé, actif)
     * @param accountId ID du compte
     * @param otpCode Code OTP à vérifier
     * @param action Type d'action
     * @return L'entité OTP si valide, null sinon
     */
    public OtpEntity verifyOtp(Long accountId, String otpCode, String action) {
        // Chercher l'OTP le plus récent pour ce compte et cette action
        List<OtpEntity> otps = otpRepository
                .find("idDemandingAccount = ?1 AND otp = ?2 AND action = ?3 AND isActive = ?4 AND isDeleted = ?5 ORDER BY createdAt DESC",
                        accountId, otpCode, action, (byte) 1, (byte) 0)
                .list();
        
        OtpEntity otp = otps.isEmpty() ? null : otps.get(0);

        if (otp == null) {
            logger.warn("OTP non trouvé pour accountId: {}, code: {}, action: {}", accountId, otpCode, action);
            return null;
        }

        // Vérifier si déjà utilisé
        if (otp.getUsedDate() != null) {
            logger.warn("OTP déjà utilisé pour accountId: {}, code: {}", accountId, otpCode);
            return null;
        }

        // Vérifier expiration
        Timestamp now = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        if (otp.getValidityDate().before(now)) {
            logger.warn("OTP expiré pour accountId: {}, code: {}", accountId, otpCode);
            return null;
        }

        logger.info("OTP valide pour accountId: {}, code: {}", accountId, otpCode);
        return otp;
    }

    /**
     * Marque un OTP comme utilisé
     * @param otp Entité OTP à marquer comme utilisé
     */
    @Transactional
    public void markAsUsed(OtpEntity otp) {
        Timestamp now = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        otp.setUsedDate(now);
        otpRepository.persist(otp);
        logger.info("OTP marqué comme utilisé: idOtp={}", otp.getIdOtp());
    }

    /**
     * Désactive tous les OTP actifs pour un compte et une action donnée
     * (utile pour invalider les anciens OTP quand on en génère un nouveau)
     * @param accountId ID du compte
     * @param action Type d'action
     */
    @Transactional
    public void invalidatePreviousOtps(Long accountId, String action) {
        Timestamp now = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        
        otpRepository
                .find("idDemandingAccount = ?1 AND action = ?2 AND isActive = ?3 AND isDeleted = ?4 AND usedDate IS NULL",
                        accountId, action, (byte) 1, (byte) 0)
                .stream()
                .forEach(otp -> {
                    otp.setIsActive((byte) 0);
                    otp.setDeletedAt(now);
                    otp.setIsDeleted((byte) 1);
                    otpRepository.persist(otp);
                });
        
        logger.info("OTP précédents invalidés pour accountId: {}, action: {}", accountId, action);
    }
}
