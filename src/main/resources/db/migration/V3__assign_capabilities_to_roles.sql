-- V3__assign_capabilities_to_roles.sql
-- MySQL 8.0.17+ / InnoDB / utf8mb4
-- Default collation: utf8mb4_0900_ai_ci

/*
============================================================================
Assign capabilities to ADMIN
============================================================================
*/

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ADMIN'
  AND c.name IN ('APPLICATION_READ', 'APPLICATION_APPROVE', 'APPLICATION_REJECT', 'APPLICATION_CANCEL',
                 'PAYMENT_READ', 'CAMP_PERIOD_MANAGE', 'CAMP_PERIOD_LEADER_ASSIGN', 'USER_MANAGE',
                 'ROLE_MANAGE', 'CAPABILITY_MANAGE', 'REPORT_READ', 'AUDIT_LOG_READ');

/*
============================================================================
Assign capabilities to GUARDIAN
============================================================================
*/

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'GUARDIAN'
  AND c.name IN ('CAMPER_MANAGE', 'APPLICATION_READ', 'APPLICATION_CREATE', 'APPLICATION_EDIT', 'APPLICATION_SUBMIT',
                 'APPLICATION_DELETE_DRAFT', 'PAYMENT_CREATE', 'PAYMENT_READ', 'APPLICATION_DECLARATION_DOWNLOAD');

/*
============================================================================
Assign capabilities to LEADER
============================================================================
*/
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'LEADER'
  AND c.name IN ('APPLICATION_READ', 'APPLICATION_APPROVE', 'APPLICATION_REJECT');