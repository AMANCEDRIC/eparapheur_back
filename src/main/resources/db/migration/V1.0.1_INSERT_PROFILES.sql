--liquibase formatted sql

--changeset cedric:2

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

INSERT INTO profil_user (lib_profil, description, created_at, is_active, deleted, updated_at, deleted_at)
SELECT 'ROOT', 'Profil root - Accès total au système', NOW(), 1, 0, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM profil_user WHERE lib_profil = 'ROOT');

INSERT INTO profil_user (lib_profil, description, created_at, is_active, deleted, updated_at, deleted_at)
SELECT 'ADMIN', 'Profil administrateur - Gestion complète', NOW(), 1, 0, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM profil_user WHERE lib_profil = 'ADMIN');

INSERT INTO profil_user (lib_profil, description, created_at, is_active, deleted, updated_at, deleted_at)
SELECT 'MANAGER', 'Profil manager - Gestion d''équipe', NOW(), 1, 0, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM profil_user WHERE lib_profil = 'MANAGER');

INSERT INTO profil_user (lib_profil, description, created_at, is_active, deleted, updated_at, deleted_at)
SELECT 'AGENT', 'Profil agent - Utilisateur standard', NOW(), 1, 0, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM profil_user WHERE lib_profil = 'AGENT');

INSERT INTO profil_user (lib_profil, description, created_at, is_active, deleted, updated_at, deleted_at)
SELECT 'AE', 'Autorité d''enregistrement - Validation de documents', NOW(), 1, 0, NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM profil_user WHERE lib_profil = 'AE');

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;