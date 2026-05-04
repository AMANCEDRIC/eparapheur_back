--liquibase formatted sql
--changeset cedric:9

SET FOREIGN_KEY_CHECKS = 0;

-- Table pour le stockage sécurisé des clés privées
CREATE TABLE IF NOT EXISTS user_private_key (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_account              BIGINT NOT NULL,
    id_certificate          BIGINT UNSIGNED, -- Lien vers le certificat public correspondant
    
    private_key_encrypted   TEXT NOT NULL,   -- La clé privée chiffrée en Base64
    encryption_iv           VARCHAR(64),      -- Vecteur d'initialisation pour le chiffrement AES
    key_algorithm           VARCHAR(20) NOT NULL DEFAULT 'RSA',
    key_size                INT NOT NULL DEFAULT 2048,
    
    is_active               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_key_account FOREIGN KEY (id_account) REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT fk_key_cert    FOREIGN KEY (id_certificate) REFERENCES user_certificate(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ajout d'une colonne dans signature_action pour lier l'acte à la clé utilisée
ALTER TABLE signature_action 
    ADD COLUMN id_private_key BIGINT UNSIGNED AFTER id_certificate,
    ADD CONSTRAINT fk_sa_private_key FOREIGN KEY (id_private_key) REFERENCES user_private_key(id) ON DELETE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
