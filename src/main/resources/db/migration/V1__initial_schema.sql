-- V1__initial_schema.sql
-- MySQL 8 / InnoDB / utf8mb4_0900_ai_ci

/*
============================================================================
Authentication & Authorization
============================================================================
Tables for authentication (users) and authorization (roles, capabilities).
*/

CREATE TABLE roles
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE capabilities
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,

    CONSTRAINT pk_capabilities PRIMARY KEY (id),
    CONSTRAINT uk_capabilities_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE roles_capabilities
(
    role_id BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,

    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),

    CONSTRAINT fk_roles_capabilities_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_roles_capabilities_capability
        FOREIGN KEY (capability_id) REFERENCES capabilities (id)
            ON DELETE CASCADE,

    INDEX ix_roles_capabilities_capability_id (capability_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE users
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_users_deleted_not_active CHECK (
        deleted_at IS NULL OR active = 0
        ),

    INDEX ix_users_role_id (role_id),
    INDEX ix_users_deleted_at (deleted_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/*
============================================================================
Domain tables
============================================================================
Tables for business logic.
*/

CREATE TABLE guardians
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    uuid BINARY(16) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    identity_number VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_guardians PRIMARY KEY (id),
    CONSTRAINT uk_guardians_user_id UNIQUE (user_id),
    CONSTRAINT uk_guardians_uuid UNIQUE (uuid),
    CONSTRAINT uk_guardians_identity_number UNIQUE (identity_number),

    CONSTRAINT fk_guardians_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT

)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE leaders
(
    user_id BIGINT NOT NULL,
    uuid BINARY(16) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_leaders PRIMARY KEY (user_id),
    CONSTRAINT uk_leaders_uuid UNIQUE (uuid),

    CONSTRAINT fk_leaders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT

)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE campers
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid BINARY(16) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    date_of_birth DATE NOT NULL,
    street_name VARCHAR(200) NOT NULL,
    street_number VARCHAR(10) NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country_code CHAR(2) NOT NULL,
    parish VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_campers PRIMARY KEY (id),
    CONSTRAINT uk_campers_uuid UNIQUE (uuid),

    CONSTRAINT chk_campers_gender
        CHECK (gender IN ('MALE', 'FEMALE'))

)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE campers_guardians
(
    camper_id BIGINT NOT NULL,
    guardian_id BIGINT NOT NULL,
    guardian_role VARCHAR(10) NOT NULL,

    CONSTRAINT pk_campers_guardians PRIMARY KEY (camper_id, guardian_id),

    CONSTRAINT uk_campers_guardians_camper_role
        UNIQUE (camper_id, guardian_role),

    CONSTRAINT fk_campers_guardians_camper_id
        FOREIGN KEY (camper_id) REFERENCES campers (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_campers_guardians_guardian_id
        FOREIGN KEY (guardian_id) REFERENCES guardians (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_campers_guardians_role
        CHECK (guardian_role IN ('PRIMARY', 'SECONDARY')),

            INDEX ix_campers_guardians_guardian_id (guardian_id)

)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE school_grades
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    grade VARCHAR(20) NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,

    CONSTRAINT pk_school_grades PRIMARY KEY (id),
    CONSTRAINT uk_school_grades_grade UNIQUE (grade),
    CONSTRAINT uk_school_grades_display_order UNIQUE (display_order)

)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_0900_ai_ci;

