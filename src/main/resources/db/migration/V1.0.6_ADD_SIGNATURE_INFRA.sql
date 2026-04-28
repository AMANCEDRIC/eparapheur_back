--liquibase formatted sql
--changeset cedric:7

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. MISE À JOUR DES TABLES EXISTANTES
-- ============================================================

-- Mise à jour de PERSON (Ajouts KYC)
-- ALTER TABLE person 
--     ADD COLUMN num_piece VARCHAR(200) AFTER code_user,
--     ADD COLUMN verified TINYINT(1) NOT NULL DEFAULT 0 AFTER num_piece;

-- Mise à jour de DOCUMENT (Ajout Hash)
-- ALTER TABLE document 
--     ADD COLUMN document_hash VARCHAR(128) AFTER document_type;

-- Mise à jour de SIGNATURE_PROGRAM (Champs requis par CrudEndPointImpl)
-- ALTER TABLE signature_program 
--     ADD COLUMN is_active TINYINT(1) DEFAULT 1,
--     ADD COLUMN deleted TINYINT(1) DEFAULT 0,
--     ADD COLUMN deleted_at DATETIME;

-- Passage des IDs en UNSIGNED pour cohérence (Optionnel mais recommandé)
-- Note : On le fait pour les tables principales si besoin, mais attention aux FK existantes.
-- Pour l'instant, on se concentre sur les nouveaux champs et nouvelles tables.

-- ============================================================
-- 2. NOUVELLES TABLES PKI & SIGNATURE
-- ============================================================

-- Table pour les certificats (Public uniquement)
CREATE TABLE IF NOT EXISTS user_certificate (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_account              BIGINT NOT NULL, -- Doit correspondre au type de account.id

    -- Données X.509
    serial_number           VARCHAR(128)    NOT NULL UNIQUE,
    issuer                  VARCHAR(512)    NOT NULL,
    subject                 VARCHAR(512)    NOT NULL,
    certificate_pem         TEXT            NOT NULL,
    public_key_fingerprint  VARCHAR(128)    NOT NULL,

    -- Validité
    valid_from              DATETIME        NOT NULL,
    valid_until             DATETIME        NOT NULL,

    -- Classification
    certificate_type        ENUM('self_signed', 'ca_issued', 'qualified') NOT NULL,
    signature_level         ENUM('simple', 'avancee', 'qualifiee')        NOT NULL DEFAULT 'simple',
    provider                VARCHAR(100),
    hostname                VARCHAR(255),

    -- Cycle de vie
    generation_state        ENUM('pending', 'generating', 'active', 'expired', 'renewal_pending')
                            NOT NULL DEFAULT 'pending',
    renew_certificate       TINYINT(1)  NOT NULL DEFAULT 0,
    revoked_at              DATETIME,
    revocation_reason       VARCHAR(255),

    is_active               TINYINT(1)  NOT NULL DEFAULT 1,
    created_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cert_account FOREIGN KEY (id_account) REFERENCES account(id) ON DELETE RESTRICT,
    INDEX idx_cert_account (id_account),
    INDEX idx_fingerprint  (public_key_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table pour le visuel de signature (Image scan/dessinée)
CREATE TABLE IF NOT EXISTS user_signature_visual (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_account      BIGINT NOT NULL,
    visual_type     ENUM('drawn', 'typed', 'uploaded') NOT NULL,
    visual_path     VARCHAR(512)    NOT NULL,
    visual_hash     VARCHAR(128)    NOT NULL,
    label           VARCHAR(100),
    is_default      TINYINT(1)  NOT NULL DEFAULT 0,
    is_active       TINYINT(1)  NOT NULL DEFAULT 1,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT(1)  NOT NULL DEFAULT 0,
    deleted_at      DATETIME,
    CONSTRAINT fk_visual_account FOREIGN KEY (id_account) REFERENCES account(id) ON DELETE RESTRICT,
    INDEX idx_visual_account (id_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table centrale des actes de signature
CREATE TABLE IF NOT EXISTS signature_action (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_step_participant     BIGINT NOT NULL, 
    id_document             BIGINT NOT NULL,
    id_certificate          BIGINT UNSIGNED,
    id_visual               BIGINT UNSIGNED,
    id_otp                  BIGINT NOT NULL, 

    action_type             ENUM('signature', 'paraphage', 'validation') NOT NULL,
    signature_level         ENUM('simple', 'avancee', 'qualifiee')       NOT NULL DEFAULT 'simple',

    document_hash_before    VARCHAR(128) NOT NULL,
    signature_value         TEXT,

    -- Position du visuel
    signature_page          SMALLINT UNSIGNED,
    signature_x             DECIMAL(8,2),
    signature_y             DECIMAL(8,2),
    signature_width         DECIMAL(8,2),
    signature_height        DECIMAL(8,2),

    ip_address              VARCHAR(45)  NOT NULL,
    user_agent              VARCHAR(512),
    geolocation             VARCHAR(128),

    signed_at               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status                  ENUM('completed', 'rejected', 'revoked') NOT NULL DEFAULT 'completed',
    rejection_reason        TEXT,

    CONSTRAINT fk_sa_participant FOREIGN KEY (id_step_participant) REFERENCES step_participant(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sa_document    FOREIGN KEY (id_document)         REFERENCES document(id)         ON DELETE RESTRICT,
    CONSTRAINT fk_sa_certificate FOREIGN KEY (id_certificate)      REFERENCES user_certificate(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sa_visual      FOREIGN KEY (id_visual)           REFERENCES user_signature_visual(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sa_otp         FOREIGN KEY (id_otp)              REFERENCES otp(id_otp)          ON DELETE RESTRICT,
    
    -- UNICITÉ : Un participant signe un document spécifique une seule fois par étape
    UNIQUE KEY uq_participant_document (id_step_participant, id_document),
    INDEX idx_sa_document (id_document),
    INDEX idx_sa_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table pour les documents finaux signés
CREATE TABLE IF NOT EXISTS signed_document (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_document         BIGINT NOT NULL,
    id_program          BIGINT NOT NULL,
    id_step             BIGINT NOT NULL,
    signed_path         VARCHAR(512)    NOT NULL,
    signed_hash         VARCHAR(128)    NOT NULL,
    file_size           BIGINT UNSIGNED NOT NULL,
    signatures_count    INT UNSIGNED    NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_signed_doc_doc     FOREIGN KEY (id_document) REFERENCES document(id)          ON DELETE RESTRICT,
    CONSTRAINT fk_signed_doc_program FOREIGN KEY (id_program)  REFERENCES signature_program(id) ON DELETE RESTRICT,
    CONSTRAINT fk_signed_doc_step    FOREIGN KEY (id_step)     REFERENCES program_step(id)      ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table immuable pour le journal d'audit (Audit Trail)
CREATE TABLE IF NOT EXISTS signature_audit_log (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_program              BIGINT,
    id_step                 BIGINT,
    id_account              BIGINT,
    id_signature_action     BIGINT UNSIGNED,
    event_type              ENUM(
                                'document_viewed',
                                'document_downloaded',
                                'otp_requested',
                                'otp_verified',
                                'certificate_attached',
                                'certificate_revoked',
                                'signature_started',
                                'signature_completed',
                                'signature_rejected',
                                'signature_revoked',
                                'program_completed',
                                'program_cancelled'
                            ) NOT NULL,
    event_data              JSON,
    ip_address              VARCHAR(45)  NOT NULL,
    user_agent              VARCHAR(512),
    entry_hash              VARCHAR(128) NOT NULL, -- Chaînage anti-falsification
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_program (id_program),
    INDEX idx_audit_event   (event_type)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
