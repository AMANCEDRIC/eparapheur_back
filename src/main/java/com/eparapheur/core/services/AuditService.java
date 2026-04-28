package com.eparapheur.core.services;

import com.eparapheur.db.entities.SignatureAuditLogEntity;
import com.eparapheur.db.repositories.SignatureAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Map;

@ApplicationScoped
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Inject
    SignatureAuditLogRepository auditLogRepository;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Enregistre un événement dans le journal d'audit avec chaînage de hash.
     */
    @Transactional
    public void logEvent(String eventType, Map<String, Object> data, Long accountId, 
                         Long programId, Long stepId, String ipAddress, String userAgent) {
        try {
            SignatureAuditLogEntity lastEntry = auditLogRepository.find("ORDER BY id DESC").firstResult();
            String previousHash = (lastEntry != null) ? lastEntry.getEntryHash() : "GENESIS_BLOCK";

            SignatureAuditLogEntity entry = new SignatureAuditLogEntity();
            entry.setEventType(eventType);
            entry.setEventData(objectMapper.writeValueAsString(data));
            entry.setIdAccount(accountId);
            entry.setIdProgram(programId);
            entry.setIdStep(stepId);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            entry.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // Calcul du hash chaîné : SHA-256(previousHash + eventType + eventData + timestamp)
            String rawContent = previousHash + "|" + eventType + "|" + entry.getEventData() + "|" + entry.getCreatedAt().getTime();
            entry.setEntryHash(calculateHash(rawContent));

            auditLogRepository.persist(entry);
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'événement d'audit", e);
            // On ne bloque pas le flux principal, mais on log l'erreur critique
        }
    }

    private String calculateHash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
