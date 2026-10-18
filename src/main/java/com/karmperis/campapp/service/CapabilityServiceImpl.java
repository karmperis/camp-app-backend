package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.CapabilityEditDTO;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.model.Capability;
import com.karmperis.campapp.repository.CapabilityRepository;
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
 * Implementation of the {@link ICapabilityService} interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CapabilityServiceImpl implements ICapabilityService {
    private final CapabilityRepository capabilityRepository;
    private final CapabilityMapper capabilityMapper;

    /**
     * Saves a new capability.
     *
     * @param dto the data transfer object containing the capability information
     * @return the saved capability
     * @throws EntityAlreadyExistsException   if a capability with the same name already exists, including a soft-deleted capability
     * @throws EntityInvalidArgumentException if the capability data is invalid
     */
    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public CapabilityReadOnlyDTO saveCapability(CapabilityInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (dto == null) {
            throw new EntityInvalidArgumentException("Capability", "Capability data cannot be null");
        }

        validateCapabilityName(dto.name());
        validateCapabilityDescription(dto.description());

        log.info("Attempting to save new capability with name: {} and description: {}", dto.name(), dto.description());

        if (capabilityRepository.existsByName(dto.name())) {
            throw new EntityAlreadyExistsException("Capability", "Capability with name " + dto.name() + " already exists");
        }

        Capability capability = capabilityMapper.mapToCapabilityEntity(dto);
        Capability savedCapability;

        try {
            savedCapability = capabilityRepository.saveAndFlush(capability);
        } catch (DataIntegrityViolationException e) {
            if (isCapabilityNameConflict(e)) {
                throw new EntityAlreadyExistsException("Capability", "Capability with name " + dto.name() + " already exists");
            }

            throw e;
        }

        UUID capabilityUuid = savedCapability.getUuid();

        afterCommit(() ->
                log.info("Capability saved successfully with UUID: {}", capabilityUuid)
        );

        return capabilityMapper.mapToCapabilityReadOnlyDTO(savedCapability);
    }

    /**
     * Finds a non-soft-deleted capability by its UUID.
     *
     * @param uuid the UUID of the capability to find
     * @return the found capability
     * @throws EntityNotFoundException        if the capability is not found
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    @Override
    @Transactional(readOnly = true)
    public CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        log.info("Attempting to find capability by UUID: {}", uuid);

        validateCapabilityUuid(uuid);

        return capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)
                .map(capabilityMapper::mapToCapabilityReadOnlyDTO)
                .orElseThrow(() -> new EntityNotFoundException("Capability", "Capability with UUID " + uuid + " not found"));
    }

    @Override
    public List<CapabilityReadOnlyDTO> findAllCapabilities() {
        return List.of();
    }

    @Override
    public CapabilityReadOnlyDTO updateCapability(UUID uuid, CapabilityEditDTO dto) throws EntityNotFoundException, EntityInvalidArgumentException {
        return null;
    }

    @Override
    public void softDeleteCapabilityByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException, OperationNotAllowedException {

    }

    @Override
    public List<CapabilityReadOnlyDTO> findAllSoftDeletedCapabilities() {
        return List.of();
    }

    /**
     * Validates the name for a capability.
     *
     * @param name the name of the capability
     * @throws EntityInvalidArgumentException if the capability name is invalid
     */
    private void validateCapabilityName(String name) throws EntityInvalidArgumentException {

        if (name == null || name.isBlank()) {
            throw new EntityInvalidArgumentException("Capability", "Capability name cannot be null or blank");
        }

        if (name.length() < 4 || name.length() > 100) {
            throw new EntityInvalidArgumentException("Capability", "Capability name must contain between 4 and 100 characters");
        }
    }

    /**
     * Validates the description for a capability.
     *
     * @param description the optional capability description
     * @throws EntityInvalidArgumentException if the capability description is invalid
     */
    private void validateCapabilityDescription(String description) throws EntityInvalidArgumentException {

        if (description != null && description.length() > 255) {
            throw new EntityInvalidArgumentException("Capability", "Capability description cannot exceed 255 characters");
        }
    }

    /**
     * Validates that the given UUID is not null.
     *
     * @param uuid the capability UUID to validate
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    private void validateCapabilityUuid(UUID uuid) throws EntityInvalidArgumentException {

        if (uuid == null) {
            throw new EntityInvalidArgumentException("Capability", "Capability UUID cannot be null");
        }
    }

    /**
     * Checks if the given exception is a capability name conflict.
     *
     * @param exception the exception to check
     * @return true if the exception is a capability name conflict, false otherwise
     */
    private boolean isCapabilityNameConflict(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                return "uk_capabilities_name".equals(constraintName) || "capabilities.uk_capabilities_name".equals(constraintName);
            }

            cause = cause.getCause();
        }

        return false;
    }

    /**
     * Registers an action to run after a successful transaction commit.
     *
     * @param action the action to execute
     */
    private void afterCommit(Runnable action) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
}