package com.karmperis.campapp.api;

import com.karmperis.campapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.campapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.campapp.core.exceptions.ValidationException;
import com.karmperis.campapp.dto.ErrorResponseDTO;
import com.karmperis.campapp.dto.RoleInsertDTO;
import com.karmperis.campapp.dto.RoleReadOnlyDTO;
import com.karmperis.campapp.dto.ValidationErrorResponseDTO;
import com.karmperis.campapp.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
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
}