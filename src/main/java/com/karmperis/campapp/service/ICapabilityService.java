package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.CapabilityEditDTO;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing capabilities.
 */

public interface ICapabilityService {

    /**
     * Creates a new capability.
     *
     * @param dto the data transfer object containing the capability information
     * @return the created capability
     * @throws EntityAlreadyExistsException   if the name is already used, including by a soft-deleted capability
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    CapabilityReadOnlyDTO saveCapability(CapabilityInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    /**
     * Finds a non-soft-deleted capability by its UUID.
     *
     * @param uuid the UUID of the capability to find
     * @return the found capability
     * @throws EntityNotFoundException        if the capability is not found
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Finds all non-soft-deleted capabilities, ordered by name in ascending order.
     *
     * @return the capability data transfer objects or an empty list if none are found
     */
    List<CapabilityReadOnlyDTO> findAllCapabilities();

    /**
     * Updates the description of a non-soft-deleted capability.
     *
     * @param uuid the UUID of the capability to update
     * @param dto  the data transfer object containing the updated capability information
     * @return the updated capability
     * @throws EntityNotFoundException        if the capability is not found
     * @throws EntityInvalidArgumentException if the input is invalid
     */
    CapabilityReadOnlyDTO updateCapability(UUID uuid, CapabilityEditDTO dto)
            throws EntityNotFoundException, EntityInvalidArgumentException;

    /**
     * Soft-deletes a capability by its UUID.
     *
     * @param uuid the UUID of the capability to soft-delete
     * @throws EntityNotFoundException        if the capability is not found
     * @throws EntityInvalidArgumentException if the input is invalid
     * @throws OperationNotAllowedException   if a business rule prevents the operation
     */
    void softDeleteCapabilityByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException, OperationNotAllowedException;

    /**
     * Finds all soft-deleted capabilities, ordered by name in ascending order.
     *
     * @return the capability data transfer objects or an empty list if none are found
     */
    List<CapabilityReadOnlyDTO> findAllSoftDeletedCapabilities();
}