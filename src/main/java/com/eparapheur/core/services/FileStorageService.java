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
    
    @ConfigProperty(name = "quarkus.file.storage.path", defaultValue = "./uploads")
    String storagePath;
    
    @ConfigProperty(name = "quarkus.file.storage.max-file-size", defaultValue = "10485760")
    Long maxFileSize;
    
    private Path baseStoragePath;

    public enum StorageType {
        ORIGINAL("documents/original"),
        SIGNED("documents/signed"),
        VISUAL("visuals"),
        PROOFS("proofs");

        private final String subPath;

        StorageType(String subPath) {
            this.subPath = subPath;
        }

        public String getSubPath() {
            return subPath;
        }
    }
    
    @PostConstruct
    void init() {
        try {
            baseStoragePath = Paths.get(storagePath).toAbsolutePath();
            
            // Créer les répertoires de base s'ils n'existent pas
            for (StorageType type : StorageType.values()) {
                Path path = baseStoragePath.resolve(type.getSubPath());
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    logger.info("Répertoire de stockage créé: {}", path);
                }
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
     * @param type Type de stockage (ORIGINAL, SIGNED, VISUAL)
     * @return Chemin relatif du fichier sauvegardé (format: TYPE/YYYY/MM/dd/filename)
     */
    public String saveBase64File(String fileName, String base64Content, StorageType type) {
        try {
            // 1. Décoder le base64
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);
            
            // 2. Valider la taille
            if (fileBytes.length > maxFileSize) {
                throw new IllegalArgumentException(
                    String.format("Fichier trop volumineux: %d bytes (max: %d)", 
                        fileBytes.length, maxFileSize));
            }
            
            // 3. Générer un nom de fichier unique
            String uniqueFileName = generateUniqueFileName(fileName);
            
            // 4. Organiser par type et date
            String dateFolder = getCurrentDateFolder();
            Path targetFolder = baseStoragePath.resolve(type.getSubPath()).resolve(dateFolder);
            
            if (!Files.exists(targetFolder)) {
                Files.createDirectories(targetFolder);
            }
            
            // 5. Chemin complet du fichier
            Path filePath = targetFolder.resolve(uniqueFileName);
            
            // 6. Sauvegarder le fichier
            Files.write(filePath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            logger.info("Fichier sauvegardé [{}]: {}", type, filePath);
            
            // 7. Retourner le chemin relatif incluant le sous-chemin du type
            // Format: documents/original/2026/04/28/filename.pdf
            return type.getSubPath() + "/" + dateFolder + "/" + uniqueFileName;
            
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
     * @param relativePath Chemin relatif (format: TYPE/YYYY/MM/dd/filename)
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

    public Path getAbsolutePath(String relativePath) {
        return baseStoragePath.resolve(relativePath);
    }
}

