package com.karmperis.campapp.api;

import com.karmperis.campapp.core.exceptions.*;
import com.karmperis.campapp.dto.*;
import com.karmperis.campapp.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing roles.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleRestController {

    private final IRoleService roleService;

    /**
     * Creates a new role.
     *
     * @param roleInsertDTO the data for the new role
     * @param bindingResult the DTO validation results
     * @return a 201 Created response containing the role DTO and its Location header
     * @throws ValidationException            if DTO validation fails
     * @throws EntityAlreadyExistsException   if the name is already used, including by a soft-deleted role
     * @throws EntityInvalidArgumentException if service-level validation fails
     */
    @Operation(
            summary = "Create a new role",
            description = "Creates a new role in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or input data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    anyOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Role already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<RoleReadOnlyDTO> createRole(@Valid @RequestBody RoleInsertDTO roleInsertDTO, BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Role", "Invalid role data", bindingResult);
        }

        RoleReadOnlyDTO roleReadOnlyDTO = roleService.saveRole(roleInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{uuid}")
                .buildAndExpand(roleReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(roleReadOnlyDTO);
    }

    /**
     * Retrieves a non-soft-deleted role by its UUID.
     *
     * @param uuid the UUID of the role to retrieve
     * @return a 200 OK response containing the role DTO
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the UUID is null
     */
    @Operation(
            summary = "Get role by UUID",
            description = "Retrieves a non-soft-deleted role by its UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Role found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<RoleReadOnlyDTO> getRoleByUuid(@PathVariable("uuid") UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        return ResponseEntity.ok(roleService.findRoleByUuid(uuid));
    }

    /**
     * Returns a list of all non-soft-deleted roles sorted by name in ascending order.
     *
     * @return HTTP 200 with a list of roles
     */
    @Operation(
            summary = "Get all roles",
            description = "Returns a list of all non-soft-deleted roles sorted by name in ascending order."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully, or an empty list if none are found",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            )
    )
    @GetMapping
    public ResponseEntity<List<RoleReadOnlyDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRoles());
    }

    /**
     * Updates the name of a non-soft-deleted role by its UUID.
     *
     * @param uuid          the UUID of the role to update
     * @param roleEditDTO   the DTO containing the updated role name
     * @param bindingResult the result of DTO validation
     * @return HTTP 200 with the updated role DTO
     * @throws ValidationException            if DTO validation fails
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws OperationNotAllowedException   if a built-in role is being renamed
     * @throws EntityAlreadyExistsException   if the name is used by another role,
     *                                        including a soft-deleted role
     * @throws EntityInvalidArgumentException if service input validation fails
     */
    @Operation(
            summary = "Update an existing role",
            description = "Updates the name of a non-soft-deleted role by its UUID. " + "Built-in roles cannot be renamed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Role updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or input data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    anyOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role does not exist or is soft-deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Role name is already in use or a built-in role cannot be renamed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<RoleReadOnlyDTO> updateRole(@PathVariable("uuid") UUID uuid,
                                                      @Valid @RequestBody RoleEditDTO roleEditDTO,
                                                      BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, OperationNotAllowedException,
            EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Role", "Invalid role update data", bindingResult);
        }

        RoleReadOnlyDTO updatedRoleDto = roleService.updateRole(uuid, roleEditDTO);
        return ResponseEntity.ok(updatedRoleDto);
    }

    /**
     * Soft deletes a role by its UUID.
     *
     * @param uuid the UUID of the role to soft-delete
     * @return HTTP 204 with no content
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if service input validation fails
     * @throws OperationNotAllowedException   if a built-in role is being soft-deleted
     */
    @Operation(
            summary = "Soft delete a role",
            description = "Marks a non-soft-deleted role as deleted. " + "Built-in roles cannot be deleted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Role successfully soft-deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role does not exist or is already soft-deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Built-in roles cannot be deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteRole(@PathVariable("uuid") UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException, OperationNotAllowedException {

        roleService.softDeleteRoleByUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns a non-soft-deleted capability to a non-soft-deleted role.
     *
     * @param roleUuid       the UUID of the role to assign the capability to
     * @param capabilityUuid the UUID of the capability to assign
     * @return HTTP 204 with no content
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityAlreadyExistsException   if the capability is already assigned to the role
     * @throws EntityInvalidArgumentException if either UUID is null
     */
    @Operation(
            summary = "Assign capability to role",
            description = "Assigns a non-soft-deleted capability to a non-soft-deleted role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Capability successfully assigned to role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role or capability UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role or capability does not exist or is soft-deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Capability already assigned to role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/{roleUuid}/capabilities/{capabilityUuid}")
    public ResponseEntity<Void> assignCapability(@PathVariable("roleUuid") UUID roleUuid, @PathVariable("capabilityUuid") UUID capabilityUuid)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        roleService.assignCapabilityToRole(roleUuid, capabilityUuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes a non-soft-deleted assigned capability from a non-soft-deleted role.
     *
     * @param roleUuid       the UUID of the role to remove the capability from
     * @param capabilityUuid the UUID of the capability to remove
     * @return HTTP 204 with no content
     * @throws EntityNotFoundException        if the role or capability does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if either UUID is null or the capability is not assigned to the role
     */
    @Operation(
            summary = "Remove capability from role",
            description = "Removes a non-soft-deleted assigned capability from a non-soft-deleted role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Capability successfully removed from role",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format or capability is not assigned to role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role or capability does not exist or is soft-deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @DeleteMapping("/{roleUuid}/capabilities/{capabilityUuid}")
    public ResponseEntity<Void> removeCapability(@PathVariable("roleUuid") UUID roleUuid, @PathVariable("capabilityUuid") UUID capabilityUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        roleService.removeCapabilityFromRole(roleUuid, capabilityUuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all non-soft-deleted capabilities assigned to a non-soft-deleted role,
     * sorted by name in ascending order.
     *
     * @param roleUuid the UUID of the role
     * @return HTTP 200 with the capability DTOs, or an empty list if none are found
     * @throws EntityNotFoundException        if the role does not exist or is soft-deleted
     * @throws EntityInvalidArgumentException if the role UUID is null
     */
    @Operation(
            summary = "Get role capabilities",
            description = "Retrieves all non-soft-deleted capabilities assigned to a non-soft-deleted role, "
                    + "sorted by name in ascending order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Capabilities retrieved successfully, or an empty list if none are found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CapabilityReadOnlyDTO.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Role does not exist or is soft-deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{roleUuid}/capabilities")
    public ResponseEntity<List<CapabilityReadOnlyDTO>> getRoleCapabilities(@PathVariable("roleUuid") UUID roleUuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        return ResponseEntity.ok(roleService.findCapabilitiesByRoleUuid(roleUuid));
    }

    /**
     * Retrieves all soft-deleted roles sorted by name in ascending order.
     *
     * @return HTTP 200 with the role DTOs, or an empty list if none are found
     */
    @Operation(
            summary = "Get all soft-deleted roles",
            description = "Retrieves all soft-deleted roles sorted by name in ascending order."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Soft-deleted roles retrieved successfully, or an empty list if none are found",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = RoleReadOnlyDTO.class)
                    )
            )
    )
    @GetMapping("/soft-deleted")
    public ResponseEntity<List<RoleReadOnlyDTO>> getAllSoftDeletedRoles() {

        return ResponseEntity.ok(roleService.findAllSoftDeletedRoles());
    }
}