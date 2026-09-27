package com.karmperis.campapp.mapper;

import com.karmperis.campapp.dto.CapabilityEditDTO;
import com.karmperis.campapp.dto.CapabilityInsertDTO;
import com.karmperis.campapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.campapp.model.Capability;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper component for {@link Capability} entities.
 */

@Mapper(componentModel = "spring")
public interface CapabilityMapper {

    /**
     * Maps a {@link CapabilityInsertDTO} to a {@link Capability} entity.
     *
     * @param dto the CapabilityInsertDTO to map
     * @return the mapped Capability entity
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    Capability mapToCapabilityEntity(CapabilityInsertDTO dto);

    /**
     * Maps a {@link Capability} entity to a {@link CapabilityReadOnlyDTO}.
     *
     * @param capability the capability entity to map
     * @return the mapped CapabilityReadOnlyDTO
     */
    CapabilityReadOnlyDTO mapToCapabilityReadOnlyDTO(Capability capability);

    /**
     * Updates an existing {@link Capability} entity from a {@link CapabilityEditDTO}.
     *
     * @param dto        the capability data to apply
     * @param capability the Capability entity to update
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "description", source = "description")
    void updateCapabilityFromDTO(CapabilityEditDTO dto, @MappingTarget Capability capability);
}