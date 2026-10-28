package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @ValueSource(ints = {4, 100})
    void saveCapability_whenNameLengthIsAtValidBoundary_shouldSucceed(int length)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("A".repeat(length), "Allows viewing applications");

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

    @ParameterizedTest
    @MethodSource("validDescriptions")
    void saveCapability_whenDescriptionIsValid_shouldSucceed(String description)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        CapabilityInsertDTO dto = new CapabilityInsertDTO("APPLICATION_READ", description);

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

    private static Stream<String> validDescriptions() {
        return Stream.of(null, "", "A".repeat(255));
    }

    @Test
    void findCapabilityByUuid_whenUuidIsNull_shouldThrowEntityInvalidArgumentException() {

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class, () -> capabilityService.findCapabilityByUuid(null));

        assertEquals("CapabilityInvalidArgument", exception.getCode());

        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void findCapabilityByUuid_whenCapabilityIsNotFound_shouldThrowEntityNotFoundException() {

        UUID uuid = UUID.randomUUID();

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> capabilityService.findCapabilityByUuid(uuid));

        assertEquals("CapabilityNotFound", exception.getCode());

        verify(capabilityRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(capabilityRepository);
        verifyNoInteractions(capabilityMapper);
    }

    @Test
    void findCapabilityByUuid_whenCapabilityExists_shouldReturnCapability()
            throws EntityNotFoundException, EntityInvalidArgumentException {

        Capability capability = new Capability();
        capability.setName("APPLICATION_READ");
        capability.setDescription("Allows viewing applications");

        UUID uuid = capability.getUuid();

        CapabilityReadOnlyDTO expected = new CapabilityReadOnlyDTO(uuid, capability.getName(), capability.getDescription());

        when(capabilityRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(capability));

        when(capabilityMapper.mapToCapabilityReadOnlyDTO(capability)).thenReturn(expected);

        CapabilityReadOnlyDTO actual = capabilityService.findCapabilityByUuid(uuid);

        assertSame(expected, actual);

        verify(capabilityRepository).findByUuidAndDeletedAtIsNull(uuid);
        verify(capabilityMapper).mapToCapabilityReadOnlyDTO(capability);

        verifyNoMoreInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void findAllCapabilities_whenNoCapabilitiesExist_shouldReturnEmptyList() {

        when(capabilityRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of());

        List<CapabilityReadOnlyDTO> actual = capabilityService.findAllCapabilities();

        assertTrue(actual.isEmpty());

        verify(capabilityRepository).findAllByDeletedAtIsNullOrderByNameAsc();
        verifyNoMoreInteractions(capabilityRepository);
        verifyNoInteractions(capabilityMapper);
    }

    @Test
    void findAllCapabilities_whenCapabilitiesExist_shouldReturnMappedList() {

        Capability firstCapability = new Capability();
        firstCapability.setName("APPLICATION_READ");
        firstCapability.setDescription("Allows viewing applications");

        Capability secondCapability = new Capability();
        secondCapability.setName("ROLE_MANAGE");
        secondCapability.setDescription("Allows managing roles");

        CapabilityReadOnlyDTO firstDto = new CapabilityReadOnlyDTO(firstCapability.getUuid(), firstCapability.getName(), firstCapability.getDescription());

        CapabilityReadOnlyDTO secondDto = new CapabilityReadOnlyDTO(secondCapability.getUuid(), secondCapability.getName(), secondCapability.getDescription());

        when(capabilityRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of(firstCapability, secondCapability));

        when(capabilityMapper.mapToCapabilityReadOnlyDTO(firstCapability)).thenReturn(firstDto);

        when(capabilityMapper.mapToCapabilityReadOnlyDTO(secondCapability)).thenReturn(secondDto);

        List<CapabilityReadOnlyDTO> actual = capabilityService.findAllCapabilities();

        assertEquals(List.of(firstDto, secondDto), actual);

        verify(capabilityRepository).findAllByDeletedAtIsNullOrderByNameAsc();
        verify(capabilityMapper).mapToCapabilityReadOnlyDTO(firstCapability);
        verify(capabilityMapper).mapToCapabilityReadOnlyDTO(secondCapability);

        verifyNoMoreInteractions(capabilityRepository, capabilityMapper);
    }
}