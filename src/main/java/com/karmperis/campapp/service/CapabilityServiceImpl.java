package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.CapabilityEditDTO;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    @Override
    public CapabilityReadOnlyDTO saveCapability(CapabilityInsertDTO dto) throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        return null;
    }

    @Override
    public CapabilityReadOnlyDTO findCapabilityByUuid(UUID uuid) throws EntityNotFoundException, EntityInvalidArgumentException {
        return null;
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