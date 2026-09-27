package com.karmperis.campapp.mapper;

import com.karmperis.campapp.dto.RoleEditDTO;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;
import com.karmperis.campapp.model.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper component for {@link Role} entities.
 */

@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Maps a {@link RoleInsertDTO} to a {@link Role} entity.
     *
     * @param dto the RoleInsertDTO to map
     * @return the mapped Role entity
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    Role mapToRoleEntity(RoleInsertDTO dto);

    /**
     * Maps a {@link Role} entity to a {@link RoleReadOnlyDTO}.
     *
     * @param role the Role entity to map
     * @return the mapped RoleReadOnlyDTO
     */
    RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role);

    /**
     * Updates an existing {@link Role} entity from a {@link RoleEditDTO}.
     *
     * @param dto  the role data to apply
     * @param role the Role entity to update
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    void updateRoleFromDTO(RoleEditDTO dto, @MappingTarget Role role);
}