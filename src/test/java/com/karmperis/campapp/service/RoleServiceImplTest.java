package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.mapper.RoleMapper;
import com.karmperis.campapp.repository.CapabilityRepository;
import com.karmperis.campapp.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

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

}