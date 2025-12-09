--liquibase formatted sql

--changeset cedric:1

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';



CREATE TABLE account (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         id_user BIGINT NOT NULL,
                         id_profil BIGINT NULL,
                         login_cmpt VARCHAR(200),
                         session_token VARCHAR(100),
                         mp_cmpt VARCHAR(255) NOT NULL,
                         created_at DATETIME,
                         deleted_at DATETIME,
                         updated_at DATETIME,
                         deleted TINYINT(1),
                         is_active TINYINT(1),
                         img_cmpt VARCHAR(200),
                         connection_attempt INT,
                         last_connected_at DATETIME,
                         PRIMARY KEY (id)
);

CREATE TABLE permission (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            lib_permis VARCHAR(255) NOT NULL,
                            code VARCHAR(100) NOT NULL,
                            deleted TINYINT(1),
                            deleted_at DATETIME,
                            is_active TINYINT(1),
                            updated_at DATETIME,
                            created_at DATETIME,
                            PRIMARY KEY (id)
);

CREATE TABLE person (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        nom_user VARCHAR(30) NOT NULL,
                        pren_user VARCHAR(50) NOT NULL,
                        tel_user VARCHAR(20),
                        code_user VARCHAR(200),
                        error_send_confirmation_email TINYINT(1) NOT NULL,
                        acceptation_CGU TINYINT(1),
                        date_acceptation_CGU DATETIME,
                        genre_user VARCHAR(10),
                        email_user VARCHAR(50) NOT NULL,
                        PRIMARY KEY (id)
);

CREATE TABLE profil_user (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             lib_profil VARCHAR(50) NOT NULL,
                             description TEXT NOT NULL,
                             created_at DATETIME,
                             is_active TINYINT(1),
                             deleted_at DATETIME,
                             updated_at DATETIME,
                             deleted TINYINT(1),
                             PRIMARY KEY (id)
);

CREATE TABLE profil_user_has_permission (
                                            id_user BIGINT NOT NULL,
                                            id_permission BIGINT NOT NULL,
                                            PRIMARY KEY (id_user, id_permission)
);

ALTER TABLE account
    ADD CONSTRAINT fk_account_person FOREIGN KEY (id_user)
        REFERENCES person (id);

ALTER TABLE account
    ADD CONSTRAINT fk_account_profil_user FOREIGN KEY (id_profil)
        REFERENCES profil_user (id);

ALTER TABLE profil_user_has_permission
    ADD CONSTRAINT fk_puhp_profil FOREIGN KEY (id_user)
        REFERENCES profil_user (id);

ALTER TABLE profil_user_has_permission
    ADD CONSTRAINT fk_puhp_permission FOREIGN KEY (id_permission)
        REFERENCES permission (id);


SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;
