--liquibase formatted sql
--changeset cedric:5

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Supprimer la FK existante
ALTER TABLE profil_user_has_permission DROP FOREIGN KEY fk_puhp_profil;

-- Renommer la colonne
ALTER TABLE profil_user_has_permission
    CHANGE COLUMN id_user id_profil BIGINT NOT NULL;

-- Recréer la PK (si nécessaire)
ALTER TABLE profil_user_has_permission
DROP PRIMARY KEY,
  ADD PRIMARY KEY (id_profil, id_permission);

-- Recréer la FK vers profil_user(id)
ALTER TABLE profil_user_has_permission
    ADD CONSTRAINT fk_puhp_profil
        FOREIGN KEY (id_profil) REFERENCES profil_user (id);

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;