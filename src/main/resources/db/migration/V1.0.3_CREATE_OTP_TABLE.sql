--liquibase formatted sql

--changeset cedric:4

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE TABLE otp (
                     id_otp BIGINT NOT NULL AUTO_INCREMENT,
                     canal ENUM('EMAIL', 'SMS', 'WHATSAPP') NOT NULL,
                     action ENUM('TWO_FACTOR_AUTH', 'CREAT_PROGRAM') NOT NULL,
                     otp VARCHAR(6) NOT NULL,
                     id_demanding_account BIGINT NOT NULL,
                     validity_date DATETIME NOT NULL,
                     used_date DATETIME NULL,
                     is_active TINYINT(1) DEFAULT 1,
                     is_deleted TINYINT(1) DEFAULT 0,
                     deleted_at DATETIME NULL,
                     created_at DATETIME NOT NULL,
                     error_send_otp TINYINT(1) DEFAULT 0,
                     details VARCHAR(45) NULL,
                     PRIMARY KEY (id_otp),
                     INDEX idx_account_action (id_demanding_account, action),
                     INDEX idx_otp_code (otp),
                     INDEX idx_validity (validity_date),
                     INDEX idx_active (is_active, is_deleted),
                     CONSTRAINT fk_otp_account FOREIGN KEY (id_demanding_account)
                         REFERENCES account (id)
                         ON DELETE CASCADE
);

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;