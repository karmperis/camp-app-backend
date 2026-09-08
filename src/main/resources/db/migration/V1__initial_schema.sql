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
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE capabilities
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,

    CONSTRAINT pk_capabilities PRIMARY KEY (id),
    CONSTRAINT uk_capabilities_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE roles_capabilities
(
    role_id       BIGINT NOT NULL,
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
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    uuid       BINARY(16)   NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role_id    BIGINT       NOT NULL,
    active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    deleted_at DATETIME(6)  NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_users_active CHECK (
        active IN (0, 1)
        ),

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
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NULL,
    uuid            BINARY(16)   NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    identity_number VARCHAR(20)  NOT NULL,
    phone_number    VARCHAR(20)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME(6)  NULL,

    CONSTRAINT pk_guardians PRIMARY KEY (id),
    CONSTRAINT uk_guardians_user_id UNIQUE (user_id),
    CONSTRAINT uk_guardians_uuid UNIQUE (uuid),
    CONSTRAINT uk_guardians_identity_number UNIQUE (identity_number),

    CONSTRAINT fk_guardians_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE leaders
(
    user_id      BIGINT       NOT NULL,
    uuid         BINARY(16)   NOT NULL,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,

    CONSTRAINT pk_leaders PRIMARY KEY (user_id),
    CONSTRAINT uk_leaders_uuid UNIQUE (uuid),

    CONSTRAINT fk_leaders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE campers
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    uuid          BINARY(16)   NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    gender        VARCHAR(10)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    street_name   VARCHAR(200) NOT NULL,
    street_number VARCHAR(10)  NULL,
    city          VARCHAR(100) NOT NULL,
    postal_code   VARCHAR(20)  NOT NULL,
    country_code  CHAR(2)      NOT NULL,
    parish        VARCHAR(100) NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    deleted_at    DATETIME(6)  NULL,

    CONSTRAINT pk_campers PRIMARY KEY (id),
    CONSTRAINT uk_campers_uuid UNIQUE (uuid),

    CONSTRAINT chk_campers_gender
        CHECK (gender IN ('MALE', 'FEMALE'))

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE campers_guardians
(
    camper_id              BIGINT      NOT NULL,
    guardian_id            BIGINT      NOT NULL,
    guardian_role          VARCHAR(10) NOT NULL,
    relationship_to_camper VARCHAR(20) NOT NULL,

    CONSTRAINT pk_campers_guardians PRIMARY KEY (camper_id, guardian_id),

    CONSTRAINT uk_campers_guardians_camper_role
        UNIQUE (camper_id, guardian_role),

    CONSTRAINT fk_campers_guardians_camper
        FOREIGN KEY (camper_id) REFERENCES campers (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_campers_guardians_guardian
        FOREIGN KEY (guardian_id) REFERENCES guardians (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_campers_guardians_role
        CHECK (guardian_role IN ('PRIMARY', 'SECONDARY')),

    CONSTRAINT chk_campers_guardians_relationship CHECK (
        relationship_to_camper IN (
                                   'FATHER',
                                   'MOTHER',
                                   'LEGAL_GUARDIAN'
            )
        ),

    INDEX ix_campers_guardians_guardian_id (guardian_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE school_grades
(
    id            BIGINT            NOT NULL AUTO_INCREMENT,
    grade         VARCHAR(20)       NOT NULL,
    display_order SMALLINT UNSIGNED NOT NULL,
    active        TINYINT(1)        NOT NULL DEFAULT 1,

    CONSTRAINT pk_school_grades PRIMARY KEY (id),
    CONSTRAINT uk_school_grades_grade UNIQUE (grade),
    CONSTRAINT uk_school_grades_display_order UNIQUE (display_order),

    CONSTRAINT chk_school_grades_active CHECK (
        active IN (0, 1)
        )

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE camp_periods
(
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    uuid                 BINARY(16)     NOT NULL,
    name                 VARCHAR(100)   NOT NULL,
    start_date           DATE           NOT NULL,
    end_date             DATE           NOT NULL,
    price_amount         DECIMAL(10, 2) NOT NULL,
    currency             CHAR(3)        NOT NULL DEFAULT 'EUR',
    allowed_gender       VARCHAR(10)    NOT NULL,
    min_school_grade_id  BIGINT         NOT NULL,
    max_school_grade_id  BIGINT         NOT NULL,
    max_capacity         INT            NOT NULL,
    application_deadline DATETIME(6)    NOT NULL,
    status               VARCHAR(10)    NOT NULL DEFAULT 'DRAFT',
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NOT NULL,
    deleted_at           DATETIME(6)    NULL,

    CONSTRAINT pk_camp_periods PRIMARY KEY (id),
    CONSTRAINT uk_camp_periods_uuid UNIQUE (uuid),

    CONSTRAINT chk_camp_periods_date_range
        CHECK (start_date <= end_date),

    CONSTRAINT chk_camp_periods_price_non_negative
        CHECK (price_amount >= 0),

    CONSTRAINT chk_camp_periods_currency
        CHECK (currency = 'EUR'),

    CONSTRAINT chk_camp_periods_allowed_gender
        CHECK (allowed_gender IN ('MALE', 'FEMALE')),

    CONSTRAINT fk_camp_periods_min_school_grade
        FOREIGN KEY (min_school_grade_id) REFERENCES school_grades (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_camp_periods_max_school_grade
        FOREIGN KEY (max_school_grade_id) REFERENCES school_grades (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_camp_periods_capacity_positive
        CHECK (max_capacity > 0),

    CONSTRAINT chk_camp_periods_status
        CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED')),

    INDEX ix_camp_periods_min_school_grade_id
        (min_school_grade_id),

    INDEX ix_camp_periods_max_school_grade_id
        (max_school_grade_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE camp_periods_leaders
(
    camp_period_id BIGINT NOT NULL,
    leader_user_id BIGINT NOT NULL,

    CONSTRAINT pk_camp_periods_leaders PRIMARY KEY (camp_period_id, leader_user_id),

    CONSTRAINT fk_camp_periods_leaders_camp_period
        FOREIGN KEY (camp_period_id) REFERENCES camp_periods (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_camp_periods_leaders_leader_user
        FOREIGN KEY (leader_user_id) REFERENCES leaders (user_id)
            ON DELETE CASCADE,

    INDEX ix_camp_periods_leaders_leader_user_id (leader_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE applications
(
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    uuid                   BINARY(16)   NOT NULL,
    camper_id              BIGINT       NOT NULL,
    camp_period_id         BIGINT       NOT NULL,
    applicant_guardian_id  BIGINT       NOT NULL,
    joint_custody          TINYINT(1)   NULL,
    wants_friend_placement TINYINT(1)   NULL,
    status                 VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    rejection_reason       VARCHAR(500) NULL,
    version                BIGINT       NOT NULL DEFAULT 0,
    submitted_at           DATETIME(6)  NULL,
    approved_at            DATETIME(6)  NULL,
    payment_deadline       DATETIME(6)  NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,
    deleted_at             DATETIME(6)  NULL,

    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT uk_applications_uuid UNIQUE (uuid),

    CONSTRAINT chk_applications_joint_custody_value CHECK (
        joint_custody IS NULL OR joint_custody IN (0, 1)
        ),

    CONSTRAINT chk_applications_joint_custody_required CHECK (
        status = 'DRAFT' OR joint_custody IS NOT NULL
        ),

    CONSTRAINT chk_applications_wants_friend_placement_value CHECK (
        wants_friend_placement IS NULL
            OR wants_friend_placement IN (0, 1)
        ),

    CONSTRAINT chk_applications_wants_friend_placement_required CHECK (
        status = 'DRAFT'
            OR wants_friend_placement IS NOT NULL
        ),

    CONSTRAINT uk_applications_camper_period
        UNIQUE (camper_id, camp_period_id),

    CONSTRAINT fk_applications_camper
        FOREIGN KEY (camper_id) REFERENCES campers (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_applications_camp_period
        FOREIGN KEY (camp_period_id) REFERENCES camp_periods (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_applications_applicant_guardian
        FOREIGN KEY (applicant_guardian_id) REFERENCES guardians (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_applications_status CHECK (
        status IN (
                   'DRAFT',
                   'SUBMITTED',
                   'APPROVED_PENDING_PAYMENT',
                   'REJECTED',
                   'PAYMENT_EXPIRED',
                   'CONFIRMED',
                   'CANCELLED',
                   'REFUNDED'
            )
        ),

    CONSTRAINT chk_applications_rejection_reason CHECK (
        (
            status = 'REJECTED'
                AND rejection_reason IS NOT NULL
                AND CHAR_LENGTH(TRIM(rejection_reason)) > 0
            )
            OR
        (
            status <> 'REJECTED'
                AND rejection_reason IS NULL
            )
        ),

    CONSTRAINT chk_applications_soft_delete_draft_only CHECK (
        deleted_at IS NULL OR status = 'DRAFT'
        ),

    INDEX ix_applications_camp_period_status
        (camp_period_id, status),
    INDEX ix_applications_applicant_guardian_id (applicant_guardian_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_camper_snapshots
(
    application_id            BIGINT       NOT NULL,
    first_name                VARCHAR(100) NOT NULL,
    last_name                 VARCHAR(100) NOT NULL,
    gender                    VARCHAR(10)  NOT NULL,
    date_of_birth             DATE         NOT NULL,
    street_name               VARCHAR(200) NOT NULL,
    street_number             VARCHAR(10)  NULL,
    city                      VARCHAR(100) NOT NULL,
    postal_code               VARCHAR(20)  NOT NULL,
    country_code              CHAR(2)      NOT NULL,
    parish                    VARCHAR(100) NULL,
    school_name               VARCHAR(200) NULL,
    completed_school_grade_id BIGINT       NULL,
    created_at                DATETIME(6)  NOT NULL,
    updated_at                DATETIME(6)  NOT NULL,

    CONSTRAINT pk_application_camper_snapshots PRIMARY KEY (application_id),

    CONSTRAINT fk_application_camper_snapshots_application
        FOREIGN KEY (application_id) REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_application_camper_snapshots_completed_school_grade
        FOREIGN KEY (completed_school_grade_id) REFERENCES school_grades (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_application_camper_snapshots_gender
        CHECK (gender IN ('MALE', 'FEMALE')),

    INDEX ix_application_camper_snapshots_completed_school_grade (completed_school_grade_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_guardian_snapshots
(
    application_id         BIGINT       NOT NULL,
    guardian_id            BIGINT       NOT NULL,
    guardian_role          VARCHAR(10)  NOT NULL,
    relationship_to_camper VARCHAR(20)  NOT NULL,
    first_name             VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,
    identity_number        VARCHAR(20)  NOT NULL,
    phone_number           VARCHAR(20)  NOT NULL,
    email                  VARCHAR(255) NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,

    CONSTRAINT pk_application_guardian_snapshots PRIMARY KEY (application_id, guardian_role),
    CONSTRAINT uk_application_guardian_snapshots_application_guardian UNIQUE (application_id, guardian_id),

    CONSTRAINT fk_application_guardian_snapshots_application
        FOREIGN KEY (application_id) REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_application_guardian_snapshots_guardian
        FOREIGN KEY (guardian_id) REFERENCES guardians (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_application_guardian_snapshots_role
        CHECK (guardian_role IN ('PRIMARY', 'SECONDARY')),

    CONSTRAINT chk_application_guardian_snapshots_relationship CHECK (
        relationship_to_camper IN (
                                   'FATHER',
                                   'MOTHER',
                                   'LEGAL_GUARDIAN'
            )
        ),

    CONSTRAINT chk_application_guardian_snapshots_primary_email_by_role CHECK (
        guardian_role = 'PRIMARY'
            AND (
            email IS NOT NULL
                AND CHAR_LENGTH(TRIM(email)) > 0
            )
            OR
        (
            guardian_role = 'SECONDARY'
                AND email IS NULL
            )
        ),

    INDEX ix_application_guardian_snapshots_guardian_id (guardian_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_signatures
(
    application_id      BIGINT          NOT NULL,
    guardian_role       VARCHAR(10)     NOT NULL,
    storage_key         VARCHAR(512)    NOT NULL,
    mime_type           VARCHAR(100)    NOT NULL,
    file_size_bytes     BIGINT UNSIGNED NOT NULL,
    checksum_hash       BINARY(32)      NOT NULL,
    signed_payload_hash BINARY(32)      NOT NULL,
    signed_at           DATETIME(6)     NOT NULL,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,

    CONSTRAINT pk_application_signatures PRIMARY KEY (application_id, guardian_role),

    CONSTRAINT fk_application_signatures_application_guardian_snapshot
        FOREIGN KEY (application_id, guardian_role)
            REFERENCES application_guardian_snapshots (application_id, guardian_role)
            ON DELETE CASCADE,

    CONSTRAINT uk_application_signatures_storage_key UNIQUE (storage_key),

    CONSTRAINT chk_application_signatures_mime_type CHECK (mime_type = 'image/png'),

    CONSTRAINT chk_application_signatures_file_size CHECK (file_size_bytes > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_pricing
(
    application_id     BIGINT         NOT NULL,
    base_price_amount  DECIMAL(10, 2) NOT NULL,
    discount_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0,
    final_price_amount DECIMAL(10, 2) NOT NULL,
    currency           CHAR(3)        NOT NULL,
    discount_code      VARCHAR(50)    NULL,
    calculated_at      DATETIME(6)    NOT NULL,
    created_at         DATETIME(6)    NOT NULL,
    updated_at         DATETIME(6)    NOT NULL,

    CONSTRAINT pk_application_pricing PRIMARY KEY (application_id),

    CONSTRAINT fk_application_pricing_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_application_pricing_base_price_amount CHECK (base_price_amount >= 0),

    CONSTRAINT chk_application_pricing_discount_amount CHECK
        (
        discount_amount >= 0
            AND
        discount_amount <= base_price_amount
        ),

    CONSTRAINT chk_application_pricing_final_price_amount CHECK
        (final_price_amount = base_price_amount - discount_amount),

    CONSTRAINT chk_application_pricing_currency CHECK (currency = 'EUR'),

    CONSTRAINT chk_application_pricing_discount_code CHECK (
        (
            discount_amount = 0
                AND discount_code IS NULL
            )
            OR
        (
            discount_amount > 0
                AND discount_code IS NOT NULL
                AND CHAR_LENGTH(TRIM(discount_code)) > 0
            )
        )

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_status_history
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT       NOT NULL,
    from_status    VARCHAR(50)  NULL,
    to_status      VARCHAR(50)  NOT NULL,
    actor_type     VARCHAR(20)  NOT NULL,
    actor_user_id  BIGINT       NULL,
    reason         VARCHAR(500) NULL,
    changed_at     DATETIME(6)  NOT NULL,

    CONSTRAINT pk_application_status_history PRIMARY KEY (id),

    CONSTRAINT fk_application_status_history_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_application_status_history_from_status CHECK (
        from_status IS NULL
            OR from_status IN (
                               'DRAFT',
                               'SUBMITTED',
                               'APPROVED_PENDING_PAYMENT',
                               'REJECTED',
                               'PAYMENT_EXPIRED',
                               'CONFIRMED',
                               'CANCELLED',
                               'REFUNDED'
            )
        ),

    CONSTRAINT chk_application_status_history_to_status CHECK (
        to_status IN (
                      'DRAFT',
                      'SUBMITTED',
                      'APPROVED_PENDING_PAYMENT',
                      'REJECTED',
                      'PAYMENT_EXPIRED',
                      'CONFIRMED',
                      'CANCELLED',
                      'REFUNDED'
            )
        ),

    CONSTRAINT chk_application_status_history_initial_status CHECK (
        from_status IS NOT NULL OR to_status = 'DRAFT'
        ),

    CONSTRAINT chk_application_status_history_status_changed CHECK (
        from_status IS NULL OR from_status <> to_status
        ),

    CONSTRAINT chk_application_status_history_actor_type CHECK (
        actor_type IN (
                       'GUARDIAN',
                       'ADMIN',
                       'SYSTEM',
                       'STRIPE'
            )
        ),

    CONSTRAINT chk_application_status_history_actor_user CHECK (
        (
            actor_type IN ('GUARDIAN', 'ADMIN')
                AND actor_user_id IS NOT NULL
            )
            OR
        (
            actor_type IN ('SYSTEM', 'STRIPE')
                AND actor_user_id IS NULL
            )
        ),

    CONSTRAINT fk_application_status_history_actor_user
        FOREIGN KEY (actor_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_application_status_history_rejection_reason CHECK (
        to_status <> 'REJECTED'
            OR (
            reason IS NOT NULL
                AND CHAR_LENGTH(TRIM(reason)) > 0
            )
        ),

    INDEX ix_application_status_history_application_changed_at
        (application_id, changed_at),

    INDEX ix_application_status_history_actor_user_id
        (actor_user_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_friend_preferences
(
    application_id   BIGINT           NOT NULL,
    preference_order TINYINT UNSIGNED NOT NULL,
    friend_full_name VARCHAR(200)     NOT NULL,
    created_at       DATETIME(6)      NOT NULL,
    updated_at       DATETIME(6)      NOT NULL,

    CONSTRAINT pk_application_friend_preferences
        PRIMARY KEY (application_id, preference_order),

    CONSTRAINT fk_application_friend_preferences_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_application_friend_preferences_order
        CHECK (preference_order BETWEEN 1 AND 3),

    CONSTRAINT chk_application_friend_preferences_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(friend_full_name)) > 0)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE application_medical_questionnaires
(
    application_id                   BIGINT        NOT NULL,
    checked_for_head_lice            TINYINT(1)    NULL,
    has_sleepwalking_history         TINYINT(1)    NULL,
    has_self_care_difficulties       TINYINT(1)    NULL,
    self_care_difficulties_details   VARCHAR(1000) NULL,
    has_allergies                    TINYINT(1)    NULL,
    allergies_details                VARCHAR(1000) NULL,
    has_anxiety_history              TINYINT(1)    NULL,
    anxiety_details                  VARCHAR(1000) NULL,
    has_behavioral_issues_history    TINYINT(1)    NULL,
    behavioral_issues_details        VARCHAR(1000) NULL,
    has_previous_camp_experience     TINYINT(1)    NULL,
    previous_camp_experience_details VARCHAR(1000) NULL,
    additional_information           VARCHAR(2000) NULL,
    created_at                       DATETIME(6)   NOT NULL,
    updated_at                       DATETIME(6)   NOT NULL,

    CONSTRAINT pk_application_medical_questionnaires
        PRIMARY KEY (application_id),

    CONSTRAINT fk_application_medical_questionnaires_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_application_medical_questionnaires_head_lice_value CHECK (
        checked_for_head_lice IS NULL
            OR checked_for_head_lice IN (0, 1)
        ),

    CONSTRAINT chk_application_medical_questionnaires_sleepwalking_value CHECK (
        has_sleepwalking_history IS NULL
            OR has_sleepwalking_history IN (0, 1)
        ),

    CONSTRAINT chk_application_medical_questionnaires_self_care_difficulties CHECK (
        (
            has_self_care_difficulties = 1
                AND self_care_difficulties_details IS NOT NULL
                AND CHAR_LENGTH(TRIM(self_care_difficulties_details)) > 0
            )
            OR
        (
            (has_self_care_difficulties = 0 OR has_self_care_difficulties IS NULL)
                AND self_care_difficulties_details IS NULL
            )
        ),

    CONSTRAINT chk_application_medical_questionnaires_allergies CHECK (
        (
            has_allergies = 1
                AND allergies_details IS NOT NULL
                AND CHAR_LENGTH(TRIM(allergies_details)) > 0
            )
            OR
        (
            (has_allergies = 0 OR has_allergies IS NULL)
                AND allergies_details IS NULL
            )
        ),

    CONSTRAINT chk_application_medical_questionnaires_anxiety_history CHECK (
        (
            has_anxiety_history = 1
                AND anxiety_details IS NOT NULL
                AND CHAR_LENGTH(TRIM(anxiety_details)) > 0
            )
            OR
        (
            (has_anxiety_history = 0 OR has_anxiety_history IS NULL)
                AND anxiety_details IS NULL
            )
        ),

    CONSTRAINT chk_application_medical_questionnaires_behavioral_issues_history CHECK (
        (
            has_behavioral_issues_history = 1
                AND behavioral_issues_details IS NOT NULL
                AND CHAR_LENGTH(TRIM(behavioral_issues_details)) > 0
            )
            OR
        (
            (has_behavioral_issues_history = 0 OR has_behavioral_issues_history IS NULL)
                AND behavioral_issues_details IS NULL
            )
        ),

    CONSTRAINT chk_application_medical_questionnaires_previous_camp_experience CHECK (
        (
            has_previous_camp_experience = 1
                AND previous_camp_experience_details IS NOT NULL
                AND CHAR_LENGTH(TRIM(previous_camp_experience_details)) > 0
            )
            OR
        (
            (has_previous_camp_experience = 0 OR has_previous_camp_experience IS NULL)
                AND previous_camp_experience_details IS NULL
            )
        )

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;