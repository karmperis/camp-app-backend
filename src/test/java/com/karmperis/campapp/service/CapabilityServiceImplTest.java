package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.model.Capability;
import com.karmperis.campapp.repository.CapabilityRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CapabilityServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
public class CapabilityServiceImplTest {

    @Mock
    private CapabilityRepository capabilityRepository;

    @Mock
    private CapabilityMapper capabilityMapper;

    private CapabilityServiceImpl capabilityService;

    @BeforeEach
    void setUp() {
        capabilityService = new CapabilityServiceImpl(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveCapability_whenDtoIsNull_shouldThrowEntityInvalidArgumentException() {

        CapabilityInsertDTO dto = null;

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> capabilityService.saveCapability(dto));

        assertEquals("CapabilityInvalidArgument", exception.getCode());

        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void saveCapability_whenNameIsNullOrBlank_shouldThrowEntityInvalidArgumentException(String name) {

        CapabilityInsertDTO dto = new CapabilityInsertDTO(name, "Allows viewing applications");

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> capabilityService.saveCapability(dto));

        assertEquals("CapabilityInvalidArgument", exception.getCode());

        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 101})
    void saveCapability_whenNameLengthIsOutOfBounds_shouldThrowEntityInvalidArgumentException(int length) {

        String name = "A".repeat(length);
        CapabilityInsertDTO dto = new CapabilityInsertDTO(name, "Allows viewing applications");

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> capabilityService.saveCapability(dto));

        assertEquals("CapabilityInvalidArgument", exception.getCode());

        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveCapability_whenDescriptionExceedsMaxLength_shouldThrowEntityInvalidArgumentException() {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", "A".repeat(256));

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> capabilityService.saveCapability(dto));

        assertEquals("CapabilityInvalidArgument", exception.getCode());

        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveCapability_whenNameAlreadyExists_shouldThrowEntityAlreadyExistsException() {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", "Allows viewing applications");

        when(capabilityRepository.existsByName(dto.name())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> capabilityService.saveCapability(dto));

        verify(capabilityRepository).existsByName(dto.name());
        verifyNoMoreInteractions(capabilityRepository);
        verifyNoInteractions(capabilityMapper);
    }

    @Test
    void saveCapability_whenIntegrityViolationIsNotNameConflict_shouldRethrowException() {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", "Allows viewing applications");

        Capability capability = new Capability();

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Unrelated integrity violation");

        when(capabilityRepository.existsByName(dto.name())).thenReturn(false);

        when(capabilityMapper.mapToCapabilityEntity(dto)).thenReturn(capability);

        when(capabilityRepository.saveAndFlush(capability)).thenThrow(databaseException);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> capabilityService.saveCapability(dto));

        assertSame(databaseException, exception);

        verify(capabilityRepository).existsByName(dto.name());
        verify(capabilityMapper).mapToCapabilityEntity(dto);
        verify(capabilityRepository).saveAndFlush(capability);

        verifyNoMoreInteractions(capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_capabilities_name", "capabilities.uk_capabilities_name"})
    void saveCapability_whenNameConstraintIsViolated_shouldThrowEntityAlreadyExistsException(String constraintName) {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", "Allows viewing applications");

        Capability capability = new Capability();

        SQLException sqlException = new SQLException("Duplicate entry", "23000", 1062);

        ConstraintViolationException constraintException = new ConstraintViolationException("Duplicate capability name", sqlException, constraintName);

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Could not save capability", constraintException);

        when(capabilityRepository.existsByName(dto.name())).thenReturn(false);

        when(capabilityMapper.mapToCapabilityEntity(dto)).thenReturn(capability);

        when(capabilityRepository.saveAndFlush(capability)).thenThrow(databaseException);

        assertThrows(EntityAlreadyExistsException.class, () -> capabilityService.saveCapability(dto));

        verify(capabilityRepository).existsByName(dto.name());
        verify(capabilityMapper).mapToCapabilityEntity(dto);
        verify(capabilityRepository).saveAndFlush(capability);

        verifyNoMoreInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveCapability_whenDataIsValid_shouldReturnSavedCapability()
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", "Allows viewing applications");

        Capability capability = new Capability();
        capability.setName(dto.name());
        capability.setDescription(dto.description());

        CapabilityReadOnlyDTO expected = new CapabilityReadOnlyDTO(
                capability.getUuid(),
                capability.getName(),
                capability.getDescription()
        );

        when(capabilityRepository.existsByName(dto.name())).thenReturn(false);

        when(capabilityMapper.mapToCapabilityEntity(dto)).thenReturn(capability);

        when(capabilityRepository.saveAndFlush(capability)).thenReturn(capability);

        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            CapabilityReadOnlyDTO actual = capabilityService.saveCapability(dto);

            assertSame(expected, actual);

            verify(capabilityRepository).existsByName(dto.name());
            verify(capabilityMapper).mapToCapabilityEntity(dto);
            verify(capabilityRepository).saveAndFlush(capability);
            verify(capabilityMapper).mapToCapabilityReadOnlyDTO(capability);

            verifyNoMoreInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}