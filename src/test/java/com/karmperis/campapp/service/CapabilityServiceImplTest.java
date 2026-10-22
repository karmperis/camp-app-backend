package com.karmperis.campapp.service;

import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.mapper.CapabilityMapper;
import com.karmperis.campapp.repository.CapabilityRepository;
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
}