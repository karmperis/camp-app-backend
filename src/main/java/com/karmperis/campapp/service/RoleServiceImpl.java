package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.dto.RoleEditDTO;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.mapper.RoleMapper;
import com.karmperis.campapp.model.Capability;
import com.karmperis.campapp.model.Role;
import com.karmperis.campapp.repository.CapabilityRepository;
import com.karmperis.campapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the {@link IRoleService} interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements IRoleService {
    private static final Set<String> BUILT_IN_ROLES = Set.of("ADMIN", "GUARDIAN", "LEADER");
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final CapabilityRepository capabilityRepository;
    private final CapabilityMapper capabilityMapper;

    /**
     * Saves a new role.
     *
     * @param dto the data transfer object containing the role information
     * @return the saved role
     * @throws EntityAlreadyExistsException   if a role with the same name already exists, including a soft-deleted role
     * @throws EntityInvalidArgumentException if the role data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public RoleReadOnlyDTO saveRole(RoleInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (dto == null) {
            throw new EntityInvalidArgumentException("Role", "Role data cannot be null");
        }

        validateRoleData(dto.name());

        log.info("Attempting to save new role with name: {}", dto.name());

        if (roleRepository.existsByName(dto.name())) {
            throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists");
        }

        Role role = roleMapper.mapToRoleEntity(dto);
        Role savedRole;

        try {
            savedRole = roleRepository.saveAndFlush(role);
        } catch (DataIntegrityViolationException e) {
            if (isRoleNameConflict(e)) {
                throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists"
                );
            }

            throw e;
        }

        UUID roleUuid = savedRole.getUuid();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Role saved successfully with UUID: {}", roleUuid);
                    }
                }
        );

        return roleMapper.mapToRoleReadOnlyDTO(savedRole);
    }

    /**
     * Finds a non soft-deleted role by its UUID.
     *
     * @param uuid the UUID of the role to find
     * @return the found role
     * @throws EntityNotFoundException        if the role is not found
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    @Override
    @Transactional(readOnly = true)
    public RoleReadOnlyDTO findRoleByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Attempting to find role by UUID: {}", uuid);

        validateUuid(uuid, "Role");

        return roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + uuid + " not found"));
    }

    /**
     * Finds all non-soft-deleted roles, ordered by name in ascending order.
     *
     * @return the role data transfer objects, or an empty list if none are found
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleReadOnlyDTO> findAllRoles() {
        log.info("Attempting to find all not deleted roles.");

        return roleRepository.findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .toList();
    }

    /**
     * Updates a non-soft-deleted role by its UUID.
     *
     * @param uuid the UUID of the role to update
     * @param dto  the data transfer object containing the updated role information
     * @return the updated role
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException   if the name is used by another role, including a soft-deleted role
     * @throws EntityInvalidArgumentException if the UUID or DTO is null or the role name is invalid
     * @throws OperationNotAllowedException   if a built-in role would be renamed
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class,
            EntityInvalidArgumentException.class, OperationNotAllowedException.class})
    public RoleReadOnlyDTO updateRole(UUID uuid, RoleEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException,
            EntityInvalidArgumentException, OperationNotAllowedException {

        log.info("Updating role with UUID: {}", uuid);

        validateUuid(uuid, "Role");

        if (dto == null) {
            throw new EntityInvalidArgumentException("Role", "Role data cannot be null");
        }

        validateRoleData(dto.name());

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + uuid + " not found"));

        if (BUILT_IN_ROLES.contains(role.getName())) {
            throw new OperationNotAllowedException("Role", "Built-in roles cannot be renamed."
            );
        }

        if (roleRepository.existsByNameAndUuidNot(dto.name(), uuid)) {
            throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists");
        }

        roleMapper.updateRoleFromDTO(dto, role);

        Role updatedRole;

        try {
            updatedRole = roleRepository.saveAndFlush(role);
        } catch (DataIntegrityViolationException e) {
            if (isRoleNameConflict(e)) {
                throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists"
                );
            }

            throw e;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Role with UUID {} updated successfully", uuid);
                    }
                }
        );

        return roleMapper.mapToRoleReadOnlyDTO(updatedRole);
    }

    /**
     * Soft-deletes a role by its UUID.
     *
     * @param uuid the UUID of the role to soft-delete
     * @throws EntityNotFoundException        if the role does not exist or is already soft-deleted
     * @throws EntityInvalidArgumentException if the UUID is null
     * @throws OperationNotAllowedException   if the role is a built-in role
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class, OperationNotAllowedException.class})
    public void softDeleteRoleByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException, OperationNotAllowedException {

        log.info("Attempting to soft-delete role with UUID: {}", uuid);

        validateUuid(uuid, "Role");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + uuid + " not found"));

        if (BUILT_IN_ROLES.contains(role.getName())) {
            throw new OperationNotAllowedException("Role", "Built-in roles cannot be deleted.");
        }

        // TODO: Reject deletion if the role is assigned to any user,
        // including inactive and soft-deleted users, once UserRepository is available.
        // For now, we'll just proceed with the deletion.
        role.softDelete();
        roleRepository.saveAndFlush(role);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Role with UUID {} soft-deleted successfully", uuid);
                    }
                }
        );

    }

    /**
     * Finds all soft-deleted roles, ordered by name in ascending order.
     *
     * @return the role data transfer objects, or an empty list if none are found
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleReadOnlyDTO> findAllSoftDeletedRoles() {
        log.info("Attempting to find all soft-deleted roles.");

        return roleRepository.findAllByDeletedAtIsNotNullOrderByNameAsc()
                .stream()
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .toList();
    }

    /**
     * Assigns a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability is not found or is soft-deleted
     * @throws EntityAlreadyExistsException   if the capability is already assigned to the role
     * @throws EntityInvalidArgumentException if any of the UUIDs are null
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        log.info("Attempting to assign capability with UUID {} to role with UUID {}", capabilityUuid, roleUuid);

        validateUuid(roleUuid, "Role");
        validateUuid(capabilityUuid, "Capability");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + roleUuid + " not found"));

        Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)
                .orElseThrow(() -> new EntityNotFoundException("Capability", "Capability with UUID " + capabilityUuid + " not found"));

        if (role.hasCapability(capability)) {
            throw new EntityAlreadyExistsException("Capability", "Capability with UUID " + capabilityUuid + " is already assigned to role with UUID " + roleUuid);
        }
        role.addCapability(capability);

        try {
            roleRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isRoleCapabilityConflict(e)) {
                throw new EntityAlreadyExistsException("Capability", "Capability with UUID " + capabilityUuid
                        + " is already assigned to role with UUID " + roleUuid);
            }
            throw e;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Capability with UUID {} assigned to role with UUID {} successfully", capabilityUuid, roleUuid);
                    }
                }
        );

    }

    /**
     * Removes a capability from a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability does not exist or is already soft-deleted
     * @throws EntityInvalidArgumentException if the role or capability UUID is null
     */
    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    public void removeCapabilityFromRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Attempting to remove capability with UUID {} from role with UUID {}", capabilityUuid, roleUuid);

        validateUuid(roleUuid, "Role");
        validateUuid(capabilityUuid, "Capability");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + roleUuid + " not found"));

        Capability capability = capabilityRepository.findByUuidAndDeletedAtIsNull(capabilityUuid)
                .orElseThrow(() -> new EntityNotFoundException("Capability", "Capability with UUID " + capabilityUuid + " not found"));

        if (!role.hasCapability(capability)) {
            throw new EntityInvalidArgumentException("Capability", "Capability with UUID " + capabilityUuid + " is not assigned to role with UUID " + roleUuid);
        }

        role.removeCapability(capability);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Capability with UUID {} removed from role with UUID {} successfully", capabilityUuid, roleUuid);
                    }
                }
        );

    }

    /**
     * Finds all non-soft-deleted capabilities assigned to a non-soft-deleted role,
     * ordered by name in ascending order.
     *
     * @param roleUuid the role UUID
     * @return the capability data transfer objects, or an empty list if none are available
     * @throws EntityNotFoundException        if the role does not exist or is already soft-deleted
     * @throws EntityInvalidArgumentException if the role UUID is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<CapabilityReadOnlyDTO> findCapabilitiesByRoleUuid(UUID roleUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Attempting to find capabilities for role with UUID: {}", roleUuid);

        validateUuid(roleUuid, "Role");

        Role role = roleRepository.findByUuidAndDeletedAtIsNull(roleUuid)
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role with UUID " + roleUuid + " not found"));

        return role.getAllCapabilities()
                .stream()
                .filter(capability -> capability.getDeletedAt() == null)
                .sorted(Comparator.comparing(Capability::getName))
                .map(capabilityMapper::mapToCapabilityReadOnlyDTO)
                .toList();
    }

    /**
     * Validates the data for a role.
     *
     * @param name the name of the role to validate
     * @throws EntityInvalidArgumentException if the role data is invalid
     */
    private void validateRoleData(String name) throws EntityInvalidArgumentException {
        if (name == null || name.isBlank()) {
            throw new EntityInvalidArgumentException("Role", "Role name cannot be blank");
        }

        int nameLength = name.length();

        if (nameLength < 4 || nameLength > 50) {
            throw new EntityInvalidArgumentException("Role", "Role name must contain between 4 and 50 characters");
        }
    }

    /**
     * Checks if the given exception is a role name conflict.
     *
     * @param exception the exception to check
     * @return true if the exception is a role name conflict, false otherwise
     */
    private boolean isRoleNameConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                return "uk_roles_name".equals(constraintName)
                        || "roles.uk_roles_name".equals(constraintName);
            }

            cause = cause.getCause();
        }

        return false;
    }

    /**
     * Checks whether the exception indicates a duplicate role-capability assignment.
     *
     * @param exception the exception to inspect
     * @return true if a duplicate assignment is identified, false otherwise
     */
    private boolean isRoleCapabilityConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                return violation.getErrorCode() == 1062
                        && "roles_capabilities.PRIMARY".equals(
                        violation.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }

    /**
     * Validates that the given UUID is not null.
     *
     * @param uuid      the UUID to validate
     * @param entityName the entity name used in the error code and message
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    private void validateUuid(UUID uuid, String entityName)
            throws EntityInvalidArgumentException {

        if (uuid == null) {
            throw new EntityInvalidArgumentException(entityName, entityName + " UUID cannot be null");
        }
    }
}