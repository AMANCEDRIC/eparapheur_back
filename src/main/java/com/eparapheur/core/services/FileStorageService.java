package com.eparapheur.core.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @ConfigProperty(name = "quarkus.file.storage.path", defaultValue = "./uploads/documents")
    String storagePath;
    
    @ConfigProperty(name = "quarkus.file.storage.max-file-size", defaultValue = "10485760")
    Long maxFileSize;
    
    private Path baseStoragePath;
    
    @PostConstruct
    void init() {
        try {
            baseStoragePath = Paths.get(storagePath).toAbsolutePath();
            
            // Créer le répertoire s'il n'existe pas
            if (!Files.exists(baseStoragePath)) {
                Files.createDirectories(baseStoragePath);
                logger.info("Répertoire de stockage créé: {}", baseStoragePath);
            } else {
                logger.info("Répertoire de stockage existant: {}", baseStoragePath);
            }
            
            // Vérifier les permissions d'écriture
            if (!Files.isWritable(baseStoragePath)) {
                throw new RuntimeException("Le répertoire de stockage n'est pas accessible en écriture: " + baseStoragePath);
            }
            
        } catch (IOException e) {
            logger.error("Erreur lors de l'initialisation du stockage de fichiers", e);
            throw new RuntimeException("Impossible d'initialiser le stockage de fichiers", e);
        }
    }
    
    /**
     * Sauvegarde un fichier encodé en base64
     * @param fileName Nom original du fichier
     * @param base64Content Contenu en base64
     * @return Chemin relatif du fichier sauvegardé (à stocker en BDD)
     */
    public String saveBase64File(String fileName, String base64Content) {
        try {
            // 1. Décoder le base64
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);
            
            // 2. Valider la taille
            if (fileBytes.length > maxFileSize) {
                throw new IllegalArgumentException(
                    String.format("Fichier trop volumineux: %d bytes (max: %d)", 
                        fileBytes.length, maxFileSize));
            }
            
            // 3. Générer un nom de fichier unique (éviter les collisions)
            String uniqueFileName = generateUniqueFileName(fileName);
            
            // 4. Organiser par date (optionnel mais recommandé)
            Path dateFolder = baseStoragePath.resolve(getCurrentDateFolder());
            if (!Files.exists(dateFolder)) {
                Files.createDirectories(dateFolder);
            }
            
            // 5. Chemin complet du fichier
            Path filePath = dateFolder.resolve(uniqueFileName);
            
            // 6. Sauvegarder le fichier
            Files.write(filePath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            logger.info("Fichier sauvegardé: {}", filePath);
            
            // 7. Retourner le chemin relatif (pour stockage en BDD)
            // Format: YYYY/MM/dd/filename.pdf
            return getCurrentDateFolder() + "/" + uniqueFileName;
            
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error("Erreur lors de la sauvegarde du fichier: {}", fileName, e);
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }
    
    /**
     * Récupère le contenu d'un fichier à partir de son chemin relatif
     * @param relativePath Chemin relatif (format: YYYY/MM/dd/filename.pdf)
     * @return Contenu du fichier en bytes
     */
    public byte[] getFileContent(String relativePath) {
        try {
            Path filePath = baseStoragePath.resolve(relativePath);
            
            if (!Files.exists(filePath)) {
                throw new IOException("Fichier non trouvé: " + relativePath);
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier: {}", relativePath, e);
            throw new RuntimeException("Erreur lors de la lecture du fichier", e);
        }
    }
    
    /**
     * Génère un nom de fichier unique pour éviter les collisions
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        String nameWithoutExt = originalFileName;
        
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot);
            nameWithoutExt = originalFileName.substring(0, lastDot);
        }
        
        // Format: nom-original_timestamp_uuid.extension
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s%s", 
            sanitizeFileName(nameWithoutExt), timestamp, uuid, extension);
    }
    
    /**
     * Nettoie le nom de fichier pour éviter les problèmes de sécurité
     */
    private String sanitizeFileName(String fileName) {
        // Supprimer les caractères dangereux
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_");  // Remplacer plusieurs _ par un seul
        
        // Limiter la longueur
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        return sanitized;
    }
    
    /**
     * Retourne le dossier de date au format YYYY/MM/dd
     */
    private String getCurrentDateFolder() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%d/%02d/%02d", 
            now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }
}

