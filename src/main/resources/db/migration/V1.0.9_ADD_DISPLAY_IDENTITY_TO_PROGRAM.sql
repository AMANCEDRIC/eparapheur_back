--liquibase formatted sql
--changeset cedric:10
ALTER TABLE signature_program ADD COLUMN display_identity BOOLEAN DEFAULT FALSE;
