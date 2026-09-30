package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.dto.RoleEditDTO;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing roles.
 */

public interface IRoleService {

    /**
     * Creates a new role.
     *
     * @param dto the data transfer object containing the role information
     * @return the saved role
     * @throws EntityAlreadyExistsException   if the name is already used, including by a soft-deleted role
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    RoleReadOnlyDTO saveRole(RoleInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Finds a non-soft-deleted role by its UUID.
     *
     * @param uuid the UUID of the role to find
     * @return the found role
     * @throws EntityNotFoundException        if the role is not found
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    RoleReadOnlyDTO findRoleByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Finds all non-soft-deleted roles, ordered by name in ascending order.
     *
     * @return the role data transfer objects or an empty list if none are found
     */
    List<RoleReadOnlyDTO> findAllRoles();

    /**
     * Updates a non-soft-deleted role by its UUID.
     *
     * @param uuid the UUID of the role to update
     * @param dto  the data transfer object containing the updated role information
     * @return the updated role
     * @throws EntityNotFoundException        if the role is not found
     * @throws EntityAlreadyExistsException   if the name is used by another role, including a soft-deleted role
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    RoleReadOnlyDTO updateRole(UUID uuid, RoleEditDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Soft deletes a role by its UUID.
     *
     * @param uuid the UUID of the role to soft-delete
     * @throws EntityNotFoundException        if the role is not found
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    void softDeleteRoleByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Finds all soft-deleted roles, ordered by name in ascending order.
     *
     * @return the role data transfer objects, or an empty list if none are found
     */
    List<RoleReadOnlyDTO> findAllSoftDeletedRoles();

    /**
     * Assigns a capability to a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException   if the capability is already assigned to the role
     * @throws EntityInvalidArgumentException if a required UUID is null
     */
    void assignCapabilityToRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Removes a capability from a role.
     *
     * @param roleUuid       the role UUID
     * @param capabilityUuid the capability UUID
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the capability is not assigned to the role
     */
    void removeCapabilityFromRole(UUID roleUuid, UUID capabilityUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Finds all non-soft-deleted capabilities assigned to a non-soft-deleted role.
     *
     * @param roleUuid the role UUID
     * @return the capability data transfer objects, or an empty list if none are assigned
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    List<CapabilityReadOnlyDTO> findCapabilitiesByRoleUuid(UUID roleUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;
}