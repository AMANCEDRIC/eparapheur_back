--liquibase formatted sql

--changeset cedric:3

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Insertion des permissions avec vérification d'existence
INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 1, 'Personnalisation de la plateforme', 'CAN_CUSTOM_PLATFORM', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 1 OR code = 'CAN_CUSTOM_PLATFORM');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 2, 'Gestion des profils administrateurs', 'CAN_MANAGE_ADMIN_PROFILES', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 2 OR code = 'CAN_MANAGE_ADMIN_PROFILES');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 3, 'Gestion des autres profils', 'CAN_MANAGE_PROFILES', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 3 OR code = 'CAN_MANAGE_PROFILES');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 4, 'Gestion des comptes utilisateur', 'CAN_MANAGE_USER_ACCOUNTS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 4 OR code = 'CAN_MANAGE_USER_ACCOUNTS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 5, 'Bloquer un compte utilisateur', 'CAN_BLOCK_USER_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 5 OR code = 'CAN_BLOCK_USER_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 6, 'Lancer la réinitialisation du mot de passe d''un utilisateur', 'CAN_RESET_USER_PASSWORD', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 6 OR code = 'CAN_RESET_USER_PASSWORD');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 7, 'Gestion des certificats', 'CAN_MANAGE_CERTIFICATES', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 7 OR code = 'CAN_MANAGE_CERTIFICATES');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 8, 'Enrôlements pour les certificats', 'CAN_MANAGE_CERTIFICATE_ENROLLMENTS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 8 OR code = 'CAN_MANAGE_CERTIFICATE_ENROLLMENTS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 9, 'Demande de Révocation des certificats', 'CAN_REQUEST_CERTIFICATE_REVOCATION', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 9 OR code = 'CAN_REQUEST_CERTIFICATE_REVOCATION');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 10, 'Revocation des certificats', 'CAN_REVOKE_CERTIFICATES', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 10 OR code = 'CAN_REVOKE_CERTIFICATES');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 11, 'Gestion des délégations', 'CAN_MANAGE_DELEGATIONS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 11 OR code = 'CAN_MANAGE_DELEGATIONS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 12, 'Gestion des types de documents', 'CAN_MANAGE_DOCUMENT_TYPES', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 12 OR code = 'CAN_MANAGE_DOCUMENT_TYPES');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 13, 'Gestion des Programmes de signature', 'CAN_MANAGE_SIGNATURE_PROGRAMS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 13 OR code = 'CAN_MANAGE_SIGNATURE_PROGRAMS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 14, 'Participer au programme', 'CAN_PARTICIPATE_IN_PROGRAM', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 14 OR code = 'CAN_PARTICIPATE_IN_PROGRAM');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 15, 'Lire un document', 'CAN_VIEW_DOCUMENT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 15 OR code = 'CAN_VIEW_DOCUMENT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 16, 'Valider un document', 'CAN_VALIDATE_DOCUMENT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 16 OR code = 'CAN_VALIDATE_DOCUMENT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 17, 'Parapher un document', 'CAN_INITIAL_DOCUMENT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 17 OR code = 'CAN_INITIAL_DOCUMENT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 18, 'Signer un document', 'CAN_SIGN_DOCUMENT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 18 OR code = 'CAN_SIGN_DOCUMENT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 19, 'Consulter, extraire des données statistiques', 'CAN_VIEW_STATISTICS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 19 OR code = 'CAN_VIEW_STATISTICS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 20, 'Création de comptes', 'CAN_CREATE_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 20 OR code = 'CAN_CREATE_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 21, 'Edition de comptes', 'CAN_EDIT_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 21 OR code = 'CAN_EDIT_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 22, 'Suppression de comptes', 'CAN_DELETE_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 22 OR code = 'CAN_DELETE_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 23, 'Lister les utilisateurs', 'CAN_LIST_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 23 OR code = 'CAN_LIST_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 24, 'Relance de la régénération des certificats', 'CAN_REINIT_CERTIFICATE_GENERATION', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 24 OR code = 'CAN_REINIT_CERTIFICATE_GENERATION');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 25, 'Création de programme de signature', 'CAN_CREATE_PROGRAM', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 25 OR code = 'CAN_CREATE_PROGRAM');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 26, 'Modification d''un programme de signature', 'CAN_EDIT_PROGRAM', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 26 OR code = 'CAN_EDIT_PROGRAM');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 27, 'Suppression d''un programme de signature', 'CAN_DELETE_PROGRAM', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 27 OR code = 'CAN_DELETE_PROGRAM');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 28, 'Lister tous les programmes de signatures de l''entreprise', 'CAN_LIST_ALL_PROGRAMS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 28 OR code = 'CAN_LIST_ALL_PROGRAMS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 29, 'Bloquer un utilisateur', 'CAN_BLOCK_USER', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 29 OR code = 'CAN_BLOCK_USER');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 30, 'Liste des permissions', 'CAN_LIST_PERMISSIONS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 30 OR code = 'CAN_LIST_PERMISSIONS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 31, 'Voir l''historique des  programmes signature', 'CAN_VIEW_PROGRAMS_HISTORY', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 31 OR code = 'CAN_VIEW_PROGRAMS_HISTORY');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 32, 'Réinitialiser le mot de passe d''un utilisateur', 'CAN_REINIT_PASSWORD', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 32 OR code = 'CAN_REINIT_PASSWORD');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 33, 'Gestion des comptes administrateurs', 'CAN_MANAGE_ADMIN_ACCOUNT', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 33 OR code = 'CAN_MANAGE_ADMIN_ACCOUNT');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 34, 'Voir les statistiques', 'CAN_VIEW_STATS', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 34 OR code = 'CAN_VIEW_STATS');

INSERT INTO permission (id, lib_permis, code, deleted, is_active, created_at, updated_at, deleted_at)
SELECT 35, 'Voir la liste des certificats échoués', 'CAN_VIEW_LIST_FAILED_CERTIFICATE', 0, 1, NOW(), NULL, NULL
    WHERE NOT EXISTS (SELECT 1 FROM permission WHERE id = 35 OR code = 'CAN_VIEW_LIST_FAILED_CERTIFICATE');

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;