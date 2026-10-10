package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.dto.RoleEditDTO;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;
import com.karmperis.campapp.mapper.RoleMapper;
import com.karmperis.campapp.model.Role;
import com.karmperis.campapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link IRoleService} interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

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
            if (!isRoleNameConflict(e)) {
                throw e;
            }

            log.warn("Role creation rejected due to a duplicate name: {}", dto.name());

            throw new EntityAlreadyExistsException("Role", "Role with name " + dto.name() + " already exists");
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
    public RoleReadOnlyDTO findRoleByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Attempting to find role by UUID: {}", uuid);

        validateUuid(uuid, "Role UUID");

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

    @Override
    public RoleReadOnlyDTO updateRole(UUID uuid, RoleEditDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException, OperationNotAllowedException {
        return null;
    }

    @Override
    public void softDeleteRoleByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException, OperationNotAllowedException {

    }

    /**
     * Finds all soft-deleted roles, ordered by name in ascending order.
     *
     * @return the role data transfer objects, or an empty list if none are found
     */
    @Override
    public List<RoleReadOnlyDTO> findAllSoftDeletedRoles() {
        log.info("Attempting to find all soft-deleted roles.");

        return roleRepository.findAllByDeletedAtIsNotNullOrderByNameAsc()
                .stream()
                .map(roleMapper::mapToRoleReadOnlyDTO)
                .toList();
    }

    @Override
    public void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid) throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

    }

    @Override
    public void removeCapabilityFromRole(UUID roleUuid, UUID capabilityUuid) throws EntityNotFoundException, EntityInvalidArgumentException {

    }

    @Override
    public List<CapabilityReadOnlyDTO> findCapabilitiesByRoleUuid(UUID roleUuid) throws EntityNotFoundException, EntityInvalidArgumentException {
        return List.of();
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
     * Validates that the given UUID is not null.
     *
     * @param uuid      the UUID to validate
     * @param fieldName the name of the field being validated
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    private void validateUuid(UUID uuid, String fieldName)
            throws EntityInvalidArgumentException {
        if (uuid == null) {
            throw new EntityInvalidArgumentException("Role", fieldName + " cannot be null");
        }
    }
}