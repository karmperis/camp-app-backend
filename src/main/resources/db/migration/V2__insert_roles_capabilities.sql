-- V2__insert_roles_capabilities.sql
-- MySQL 8.0.17+ / InnoDB / utf8mb4
-- Default collation: utf8mb4_0900_ai_ci

/*
============================================================================
Insert roles
============================================================================
*/
INSERT INTO roles(name)
VALUES ('ADMIN'),
       ('GUARDIAN'),
       ('LEADER');

/*
============================================================================
Insert capabilities
============================================================================
*/
INSERT INTO capabilities(name, description)
VALUES ('CAMPER_MANAGE', 'Create and update camper profiles and guardian relationships.'),
       ('APPLICATION_READ', 'View camper applications.'),
       ('APPLICATION_CREATE', 'Create applications for campers.'),
       ('APPLICATION_EDIT', 'Edit draft applications.'),
       ('APPLICATION_SUBMIT', 'Submit completed draft applications.'),
       ('APPLICATION_DELETE_DRAFT', 'Soft-delete draft applications.'),
       ('APPLICATION_APPROVE', 'Approve or reapprove applications.'),
       ('APPLICATION_REJECT', 'Reject applications.'),
       ('APPLICATION_CANCEL', 'Cancel applications or confirmed participation.'),
       ('PAYMENT_CREATE', 'Initiate payment attempts for applications.'),
       ('PAYMENT_READ', 'View payment attempts and refunds for applications.'),
       ('APPLICATION_DECLARATION_DOWNLOAD', 'Download application declarations.'),
       ('CAMP_PERIOD_MANAGE', 'Create and update camp periods.'),
       ('CAMP_PERIOD_LEADER_ASSIGN', 'Assign leaders to camp periods or remove existing assignments.'),
       ('USER_MANAGE', 'Manage user accounts and assign roles.'),
       ('ROLE_MANAGE', 'Manage roles and their capability assignments.'),
       ('CAPABILITY_MANAGE', 'Manage capabilities.'),
       ('REPORT_READ', 'View administrative reports.'),
       ('AUDIT_LOG_READ', 'View audit records of significant user and system actions.');