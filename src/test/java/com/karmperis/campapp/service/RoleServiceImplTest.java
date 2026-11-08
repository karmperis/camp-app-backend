package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.mapper.RoleMapper;
import com.karmperis.campapp.model.Role;
import com.karmperis.campapp.repository.CapabilityRepository;
import com.karmperis.campapp.repository.RoleRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RoleServiceImpl}.
 */

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CapabilityRepository capabilityRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private CapabilityMapper capabilityMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void saveRole_whenDtoIsNull_shouldThrowEntityInvalidArgumentException() {

        RoleInsertDTO dto = null;

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.saveRole(dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void saveRole_whenNameIsNullOrBlank_shouldThrowEntityInvalidArgumentException(String name) {

        RoleInsertDTO dto = new RoleInsertDTO(name);

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.saveRole(dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 51})
    void saveRole_whenNameLengthIsOutOfBounds_shouldThrowEntityInvalidArgumentException(int length) {

        String name = "A".repeat(length);
        RoleInsertDTO dto = new RoleInsertDTO(name);

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.saveRole(dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void saveRole_whenNameAlreadyExists_shouldThrowEntityAlreadyExistsException() {

        RoleInsertDTO dto = new RoleInsertDTO("CUSTOM_ROLE");

        when(roleRepository.existsByName(dto.name())).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> roleService.saveRole(dto));

        verify(roleRepository).existsByName(dto.name());
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void saveRole_whenIntegrityViolationIsNotNameConflict_shouldRethrowException() {

        RoleInsertDTO dto = new RoleInsertDTO("CUSTOM_ROLE");

        Role role = new Role();

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Unrelated integrity violation");

        when(roleRepository.existsByName(dto.name())).thenReturn(false);

        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);

        when(roleRepository.saveAndFlush(role)).thenThrow(databaseException);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> roleService.saveRole(dto));

        assertSame(databaseException, exception);

        verify(roleRepository).existsByName(dto.name());
        verify(roleMapper).mapToRoleEntity(dto);
        verify(roleRepository).saveAndFlush(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_roles_name", "roles.uk_roles_name"})
    void saveRole_whenNameConstraintIsViolated_shouldThrowEntityAlreadyExistsException(String constraintName) {

        RoleInsertDTO dto = new RoleInsertDTO("CUSTOM_ROLE");

        Role role = new Role();

        SQLException sqlException = new SQLException("Duplicate entry", "23000", 1062);

        ConstraintViolationException constraintException = new ConstraintViolationException("Duplicate role name", sqlException, constraintName);

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Could not save role", constraintException);

        when(roleRepository.existsByName(dto.name())).thenReturn(false);

        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);

        when(roleRepository.saveAndFlush(role)).thenThrow(databaseException);

        assertThrows(EntityAlreadyExistsException.class, () -> roleService.saveRole(dto));

        verify(roleRepository).existsByName(dto.name());
        verify(roleMapper).mapToRoleEntity(dto);
        verify(roleRepository).saveAndFlush(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveRole_whenAnotherConstraintIsViolated_shouldRethrowException() {

        RoleInsertDTO dto = new RoleInsertDTO("CUSTOM_ROLE");

        Role role = new Role();

        ConstraintViolationException constraintException =
                new ConstraintViolationException("Another constraint was violated", new SQLException("Integrity constraint violation", "23000"),
                        "some_other_constraint");

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Could not save role", constraintException);

        when(roleRepository.existsByName(dto.name())).thenReturn(false);

        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);

        when(roleRepository.saveAndFlush(role)).thenThrow(databaseException);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> roleService.saveRole(dto));

        assertSame(databaseException, exception);

        verify(roleRepository).existsByName(dto.name());
        verify(roleMapper).mapToRoleEntity(dto);
        verify(roleRepository).saveAndFlush(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void saveRole_whenDataIsValid_shouldReturnSavedRole()
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        RoleInsertDTO dto = new RoleInsertDTO("CUSTOM_ROLE");

        Role role = new Role();
        role.setName(dto.name());

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(role.getUuid(), role.getName());

        when(roleRepository.existsByName(dto.name())).thenReturn(false);

        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);

        when(roleRepository.saveAndFlush(role)).thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            RoleReadOnlyDTO actual = roleService.saveRole(dto);

            assertSame(expected, actual);

            verify(roleRepository).existsByName(dto.name());
            verify(roleMapper).mapToRoleEntity(dto);
            verify(roleRepository).saveAndFlush(role);
            verify(roleMapper).mapToRoleReadOnlyDTO(role);

            verifyNoMoreInteractions(roleRepository, roleMapper);
            verifyNoInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 50})
    void saveRole_whenNameLengthIsAtValidBoundary_shouldSucceed(int length)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        RoleInsertDTO dto = new RoleInsertDTO("A".repeat(length));

        Role role = new Role();
        role.setName(dto.name());

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(role.getUuid(), role.getName());

        when(roleRepository.existsByName(dto.name())).thenReturn(false);

        when(roleMapper.mapToRoleEntity(dto)).thenReturn(role);

        when(roleRepository.saveAndFlush(role)).thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            RoleReadOnlyDTO actual = roleService.saveRole(dto);

            assertSame(expected, actual);

            verify(roleRepository).existsByName(dto.name());
            verify(roleMapper).mapToRoleEntity(dto);
            verify(roleRepository).saveAndFlush(role);
            verify(roleMapper).mapToRoleReadOnlyDTO(role);

            verifyNoMoreInteractions(roleRepository, roleMapper);
            verifyNoInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void findRoleByUuid_whenUuidIsNull_shouldThrowEntityInvalidArgumentException() {

        UUID uuid = null;

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.findRoleByUuid(uuid));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void findRoleByUuid_whenRoleIsNotFound_shouldThrowEntityNotFoundException() {

        UUID uuid = UUID.randomUUID();

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> roleService.findRoleByUuid(uuid));

        assertEquals("RoleNotFound", exception.getCode());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }


}