--liquibase formatted sql
--changeset cedric:6

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Table pour les programmes de signature
CREATE TABLE signature_program (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   title VARCHAR(255) NOT NULL,
                                   description TEXT,
                                   id_initiator_account BIGINT NOT NULL,
                                   program_type VARCHAR(50) DEFAULT 'INTERNAL_FLOW',
                                   status ENUM('DRAFT', 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') NOT NULL DEFAULT 'DRAFT',
                                   start_date DATETIME NULL,
                                   end_date DATETIME NULL,
                                   created_at DATETIME NOT NULL,
                                   updated_at DATETIME NULL,
                                   PRIMARY KEY (id),
                                   CONSTRAINT fk_program_initiator FOREIGN KEY (id_initiator_account)
                                       REFERENCES account (id) ON DELETE RESTRICT
);

-- Table pour les étapes du programme
CREATE TABLE program_step (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              id_program BIGINT NOT NULL,
                              step_order INT NOT NULL,
                              action_type ENUM('SIGN', 'VALIDATION', 'PARAPHER') NOT NULL,
                              name VARCHAR(255) NOT NULL,
                              description TEXT,
                              required TINYINT(1) DEFAULT 1,
                              status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
                              created_at DATETIME NOT NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_step_program FOREIGN KEY (id_program)
                                  REFERENCES signature_program (id) ON DELETE CASCADE,
                              UNIQUE KEY uk_program_step_order (id_program, step_order)
);

-- Table pour les participants des étapes
CREATE TABLE step_participant (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  id_step BIGINT NOT NULL,
                                  id_account BIGINT NOT NULL,
                                  action ENUM('SIGN', 'VALIDATION', 'PARAPHER') NOT NULL,
                                  position INT DEFAULT 0,
                                  required TINYINT(1) DEFAULT 1,
                                  status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'SKIPPED') NOT NULL DEFAULT 'PENDING',
                                  created_at DATETIME NOT NULL,
                                  PRIMARY KEY (id),
                                  CONSTRAINT fk_participant_step FOREIGN KEY (id_step)
                                      REFERENCES program_step (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_participant_account FOREIGN KEY (id_account)
                                      REFERENCES account (id) ON DELETE RESTRICT,
                                  UNIQUE KEY uk_step_participant (id_step, id_account)
);

-- Table pour les documents
CREATE TABLE document (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          document_name VARCHAR(255) NOT NULL,
                          document_path VARCHAR(500) NOT NULL,
                          document_size BIGINT,
                          document_type VARCHAR(100),
                          uploaded_by_account BIGINT NOT NULL,
                          uploaded_at DATETIME NOT NULL,
                          created_at DATETIME NOT NULL,
                          PRIMARY KEY (id),
                          CONSTRAINT fk_document_uploader FOREIGN KEY (uploaded_by_account)
                              REFERENCES account (id) ON DELETE RESTRICT
);

-- Table de liaison many-to-many entre étapes et documents
CREATE TABLE step_document (
                               id_step BIGINT NOT NULL,
                               id_document BIGINT NOT NULL,
                               PRIMARY KEY (id_step, id_document),
                               CONSTRAINT fk_sd_step FOREIGN KEY (id_step)
                                   REFERENCES program_step (id) ON DELETE CASCADE,
                               CONSTRAINT fk_sd_document FOREIGN KEY (id_document)
                                   REFERENCES document (id) ON DELETE CASCADE
);

-- Table pour les commentaires (optionnel)
CREATE TABLE program_comment (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 id_program BIGINT NULL,
                                 id_step BIGINT NULL,
                                 id_account BIGINT NOT NULL,
                                 comment_text TEXT NOT NULL,
                                 created_at DATETIME NOT NULL,
                                 PRIMARY KEY (id),
                                 CONSTRAINT fk_comment_program FOREIGN KEY (id_program)
                                     REFERENCES signature_program (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_comment_step FOREIGN KEY (id_step)
                                     REFERENCES program_step (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_comment_account FOREIGN KEY (id_account)
                                     REFERENCES account (id) ON DELETE RESTRICT,
                                 CHECK ((id_program IS NOT NULL) OR (id_step IS NOT NULL))
);

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;