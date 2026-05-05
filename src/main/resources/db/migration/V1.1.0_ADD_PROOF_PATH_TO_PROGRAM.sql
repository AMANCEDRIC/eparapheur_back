--liquibase formatted sql
--changeset cedric:11
ALTER TABLE signature_program ADD COLUMN proof_path VARCHAR(512);
