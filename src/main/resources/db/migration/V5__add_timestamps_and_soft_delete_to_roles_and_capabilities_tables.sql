-- V5__add_timestamps_and_soft_delete_to_roles_and_capabilities_tables.sql
-- MySQL 8.0.17+ / InnoDB / utf8mb4
-- Default collation: utf8mb4_0900_ai_ci
-- Initialize existing rows with the migration execution time in UTC.

/*
============================================================================
Roles
============================================================================
*/
ALTER TABLE roles
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL,
    ADD COLUMN deleted_at DATETIME(6) NULL;

UPDATE roles
SET created_at = UTC_TIMESTAMP(6),
    updated_at = UTC_TIMESTAMP(6)
WHERE created_at IS NULL
  AND updated_at IS NULL;

ALTER TABLE roles
    MODIFY COLUMN created_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

/*
============================================================================
Capabilities
============================================================================
*/
ALTER TABLE capabilities
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL,
    ADD COLUMN deleted_at DATETIME(6) NULL;

UPDATE capabilities
SET created_at = UTC_TIMESTAMP(6),
    updated_at = UTC_TIMESTAMP(6)
WHERE created_at IS NULL
  AND updated_at IS NULL;

ALTER TABLE capabilities
    MODIFY COLUMN created_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;