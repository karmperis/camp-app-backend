package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.EntityNotFoundException;
import com.karmperis.campapp.core.exceptions.OperationNotAllowedException;
import com.karmperis.campapp.dto.RoleEditDTO;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.List;
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

    @Test
    void findRoleByUuid_whenRoleExists_shouldReturnRole()
            throws EntityNotFoundException, EntityInvalidArgumentException {

        Role role = new Role();

        role.setName("CUSTOM_ROLE");

        UUID uuid = role.getUuid();

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(uuid, role.getName());

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        RoleReadOnlyDTO actual = roleService.findRoleByUuid(uuid);

        assertSame(expected, actual);

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verify(roleMapper).mapToRoleReadOnlyDTO(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void findAllRoles_whenNoRolesExist_shouldReturnEmptyList() {

        when(roleRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of());

        List<RoleReadOnlyDTO> actual = roleService.findAllRoles();

        assertTrue(actual.isEmpty());

        verify(roleRepository).findAllByDeletedAtIsNullOrderByNameAsc();
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void findAllRoles_whenRolesExist_shouldReturnMappedList() {

        Role firstRole = new Role();
        firstRole.setName("ADMIN");

        Role secondRole = new Role();
        secondRole.setName("GUARDIAN");

        RoleReadOnlyDTO firstDto = new RoleReadOnlyDTO(firstRole.getUuid(), firstRole.getName());

        RoleReadOnlyDTO secondDto = new RoleReadOnlyDTO(secondRole.getUuid(), secondRole.getName());

        when(roleRepository.findAllByDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of(firstRole, secondRole));

        when(roleMapper.mapToRoleReadOnlyDTO(firstRole)).thenReturn(firstDto);

        when(roleMapper.mapToRoleReadOnlyDTO(secondRole)).thenReturn(secondDto);

        List<RoleReadOnlyDTO> actual = roleService.findAllRoles();

        assertEquals(List.of(firstDto, secondDto), actual);

        verify(roleRepository).findAllByDeletedAtIsNullOrderByNameAsc();
        verify(roleMapper).mapToRoleReadOnlyDTO(firstRole);
        verify(roleMapper).mapToRoleReadOnlyDTO(secondRole);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void findAllSoftDeletedRoles_whenNoRolesExist_shouldReturnEmptyList() {

        when(roleRepository.findAllByDeletedAtIsNotNullOrderByNameAsc()).thenReturn(List.of());

        List<RoleReadOnlyDTO> actual = roleService.findAllSoftDeletedRoles();

        assertTrue(actual.isEmpty());

        verify(roleRepository).findAllByDeletedAtIsNotNullOrderByNameAsc();
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void findAllSoftDeletedRoles_whenRolesExist_shouldReturnMappedList() {

        Role firstRole = new Role();
        firstRole.setName("CUSTOM_EDITOR");
        firstRole.softDelete();

        Role secondRole = new Role();
        secondRole.setName("CUSTOM_VIEWER");
        secondRole.softDelete();

        RoleReadOnlyDTO firstDto = new RoleReadOnlyDTO(firstRole.getUuid(), firstRole.getName());

        RoleReadOnlyDTO secondDto = new RoleReadOnlyDTO(secondRole.getUuid(), secondRole.getName());

        when(roleRepository.findAllByDeletedAtIsNotNullOrderByNameAsc()).thenReturn(List.of(firstRole, secondRole));

        when(roleMapper.mapToRoleReadOnlyDTO(firstRole)).thenReturn(firstDto);

        when(roleMapper.mapToRoleReadOnlyDTO(secondRole)).thenReturn(secondDto);

        List<RoleReadOnlyDTO> actual = roleService.findAllSoftDeletedRoles();

        assertEquals(List.of(firstDto, secondDto), actual);

        verify(roleRepository).findAllByDeletedAtIsNotNullOrderByNameAsc();
        verify(roleMapper).mapToRoleReadOnlyDTO(firstRole);
        verify(roleMapper).mapToRoleReadOnlyDTO(secondRole);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void updateRole_whenUuidIsNull_shouldThrowEntityInvalidArgumentException() {

        UUID uuid = null;
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void updateRole_whenDtoIsNull_shouldThrowEntityInvalidArgumentException() {

        UUID uuid = UUID.randomUUID();
        RoleEditDTO dto = null;

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void updateRole_whenNameIsNullOrBlank_shouldThrowEntityInvalidArgumentException(String name) {

        UUID uuid = UUID.randomUUID();
        RoleEditDTO dto = new RoleEditDTO(name);

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 51})
    void updateRole_whenNameLengthIsOutOfBounds_shouldThrowEntityInvalidArgumentException(
            int length) {

        UUID uuid = UUID.randomUUID();
        RoleEditDTO dto = new RoleEditDTO("A".repeat(length));

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void updateRole_whenRoleIsNotFound_shouldThrowEntityNotFoundException() {

        UUID uuid = UUID.randomUUID();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleNotFound", exception.getCode());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void updateRole_whenNameIsUsedByAnotherRole_shouldThrowEntityAlreadyExistsException() {

        Role role = new Role();
        role.setName("CUSTOM_VIEWER");

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(dto.name(), uuid)).thenReturn(true);


        assertThrows(EntityAlreadyExistsException.class, () -> roleService.updateRole(uuid, dto));

        assertEquals("CUSTOM_VIEWER", role.getName());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verify(roleRepository).existsByNameAndUuidNot(dto.name(), uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "GUARDIAN", "LEADER"})
    void updateRole_whenBuiltInRoleIsRenamed_shouldThrowOperationNotAllowedException(String originalName) {

        Role role = new Role();
        role.setName(originalName);

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        OperationNotAllowedException exception = assertThrows(OperationNotAllowedException.class,
                () -> roleService.updateRole(uuid, dto));

        assertEquals("RoleOperationNotAllowed", exception.getCode());
        assertEquals(originalName, role.getName());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "GUARDIAN", "LEADER"})
    void updateRole_whenBuiltInRoleNameIsUnchanged_shouldSucceed(String name)
            throws EntityNotFoundException, EntityAlreadyExistsException,
            EntityInvalidArgumentException, OperationNotAllowedException {

        Role role = new Role();
        role.setName(name);

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO(name);

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(uuid, name);

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(name, uuid)).thenReturn(false);

        when(roleRepository.saveAndFlush(role)).thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            RoleReadOnlyDTO actual = roleService.updateRole(uuid, dto);

            assertSame(expected, actual);
            assertEquals(name, role.getName());

            InOrder inOrder = inOrder(roleRepository, roleMapper);

            inOrder.verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
            inOrder.verify(roleRepository).existsByNameAndUuidNot(name, uuid);
            inOrder.verify(roleMapper).updateRoleFromDTO(dto, role);
            inOrder.verify(roleRepository).saveAndFlush(role);
            inOrder.verify(roleMapper).mapToRoleReadOnlyDTO(role);

            verifyNoMoreInteractions(roleRepository, roleMapper);
            verifyNoInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void updateRole_whenDataIsValid_shouldReturnUpdatedRole()
            throws EntityNotFoundException, EntityAlreadyExistsException,
            EntityInvalidArgumentException, OperationNotAllowedException {

        Role role = new Role();
        role.setName("CUSTOM_VIEWER");

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(uuid, dto.name());

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(dto.name(), uuid)).thenReturn(false);

        when(roleRepository.saveAndFlush(role)).thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            RoleReadOnlyDTO actual = roleService.updateRole(uuid, dto);

            assertSame(expected, actual);

            InOrder inOrder = inOrder(roleRepository, roleMapper);

            inOrder.verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
            inOrder.verify(roleRepository).existsByNameAndUuidNot(dto.name(), uuid);
            inOrder.verify(roleMapper).updateRoleFromDTO(dto, role);
            inOrder.verify(roleRepository).saveAndFlush(role);
            inOrder.verify(roleMapper).mapToRoleReadOnlyDTO(role);

            verifyNoMoreInteractions(roleRepository, roleMapper);
            verifyNoInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 50})
    void updateRole_whenNameLengthIsAtValidBoundary_shouldSucceed(int length)
            throws EntityNotFoundException, EntityAlreadyExistsException,
            EntityInvalidArgumentException, OperationNotAllowedException {

        Role role = new Role();
        role.setName("CUSTOM_VIEWER");

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("A".repeat(length));

        RoleReadOnlyDTO expected = new RoleReadOnlyDTO(uuid, dto.name());

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(dto.name(), uuid)).thenReturn(false);

        when(roleRepository.saveAndFlush(role)).thenReturn(role);

        when(roleMapper.mapToRoleReadOnlyDTO(role)).thenReturn(expected);

        TransactionSynchronizationManager.initSynchronization();

        try {
            RoleReadOnlyDTO actual = roleService.updateRole(uuid, dto);

            assertSame(expected, actual);

            InOrder inOrder = inOrder(roleRepository, roleMapper);

            inOrder.verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
            inOrder.verify(roleRepository).existsByNameAndUuidNot(dto.name(), uuid);
            inOrder.verify(roleMapper).updateRoleFromDTO(dto, role);
            inOrder.verify(roleRepository).saveAndFlush(role);
            inOrder.verify(roleMapper).mapToRoleReadOnlyDTO(role);

            verifyNoMoreInteractions(roleRepository, roleMapper);
            verifyNoInteractions(capabilityRepository, capabilityMapper);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_roles_name", "roles.uk_roles_name"})
    void updateRole_whenNameConstraintIsViolated_shouldThrowEntityAlreadyExistsException(String constraintName) {

        Role role = new Role();
        role.setName("CUSTOM_VIEWER");

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        ConstraintViolationException constraintException = new ConstraintViolationException("Duplicate role name",
                new SQLException("Duplicate entry", "23000", 1062), constraintName);

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Could not update role",
                constraintException);

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(dto.name(), uuid)).thenReturn(false);

        when(roleRepository.saveAndFlush(role)).thenThrow(databaseException);

        assertThrows(EntityAlreadyExistsException.class, () -> roleService.updateRole(uuid, dto));

        InOrder inOrder = inOrder(roleRepository, roleMapper);

        inOrder.verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        inOrder.verify(roleRepository).existsByNameAndUuidNot(dto.name(), uuid);
        inOrder.verify(roleMapper).updateRoleFromDTO(dto, role);
        inOrder.verify(roleRepository).saveAndFlush(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void updateRole_whenAnotherConstraintIsViolated_shouldRethrowException() {

        Role role = new Role();
        role.setName("CUSTOM_VIEWER");

        UUID uuid = role.getUuid();
        RoleEditDTO dto = new RoleEditDTO("CUSTOM_EDITOR");

        ConstraintViolationException constraintException = new ConstraintViolationException(
                "Another constraint was violated", new SQLException("Integrity constraint violation", "23000"),
                "some_other_constraint");

        DataIntegrityViolationException databaseException = new DataIntegrityViolationException("Could not update role",
                constraintException);

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndUuidNot(dto.name(), uuid)).thenReturn(false);

        when(roleRepository.saveAndFlush(role)).thenThrow(databaseException);


        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> roleService.updateRole(uuid, dto));

        assertSame(databaseException, exception);

        InOrder inOrder = inOrder(roleRepository, roleMapper);

        inOrder.verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        inOrder.verify(roleRepository).existsByNameAndUuidNot(dto.name(), uuid);
        inOrder.verify(roleMapper).updateRoleFromDTO(dto, role);
        inOrder.verify(roleRepository).saveAndFlush(role);

        verifyNoMoreInteractions(roleRepository, roleMapper);
        verifyNoInteractions(capabilityRepository, capabilityMapper);
    }

    @Test
    void softDeleteRoleByUuid_whenUuidIsNull_shouldThrowEntityInvalidArgumentException() {

        UUID uuid = null;

        EntityInvalidArgumentException exception = assertThrows(EntityInvalidArgumentException.class,
                () -> roleService.softDeleteRoleByUuid(uuid));

        assertEquals("RoleInvalidArgument", exception.getCode());

        verifyNoInteractions(roleRepository, roleMapper, capabilityRepository, capabilityMapper);
    }

    @Test
    void softDeleteRoleByUuid_whenRoleIsNotFound_shouldThrowEntityNotFoundException() {

        UUID uuid = UUID.randomUUID();

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> roleService.softDeleteRoleByUuid(uuid));

        assertEquals("RoleNotFound", exception.getCode());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "GUARDIAN", "LEADER"})
    void softDeleteRoleByUuid_whenRoleIsBuiltIn_shouldThrowOperationNotAllowedException(String name) {

        Role role = new Role();
        role.setName(name);

        UUID uuid = role.getUuid();

        when(roleRepository.findByUuidAndDeletedAtIsNull(uuid)).thenReturn(Optional.of(role));

        OperationNotAllowedException exception = assertThrows(OperationNotAllowedException.class,
                () -> roleService.softDeleteRoleByUuid(uuid));

        assertEquals("RoleOperationNotAllowed", exception.getCode());
        assertNull(role.getDeletedAt());

        verify(roleRepository).findByUuidAndDeletedAtIsNull(uuid);
        verifyNoMoreInteractions(roleRepository);

        verifyNoInteractions(roleMapper, capabilityRepository, capabilityMapper);
    }

}