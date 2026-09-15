-- V4__add_uuid_to_roles_and_capabilities_tables.sql
-- MySQL 8.0.17+ / InnoDB / utf8mb4
-- Default collation: utf8mb4_0900_ai_ci

/*
============================================================================
Roles
============================================================================
*/
ALTER TABLE roles
    ADD COLUMN uuid BINARY(16) NULL AFTER id;

UPDATE roles
SET uuid = UUID_TO_BIN(UUID())
WHERE uuid IS NULL;

ALTER TABLE roles
    MODIFY COLUMN uuid BINARY(16) NOT NULL,
    ADD CONSTRAINT uk_roles_uuid UNIQUE (uuid);

/*
============================================================================
Capabilities
============================================================================
*/
ALTER TABLE capabilities
    ADD COLUMN uuid BINARY(16) NULL AFTER id;

UPDATE capabilities
SET uuid = UUID_TO_BIN(UUID())
WHERE uuid IS NULL;

ALTER TABLE capabilities
    MODIFY COLUMN uuid BINARY(16) NOT NULL,
    ADD CONSTRAINT uk_capabilities_uuid UNIQUE (uuid);