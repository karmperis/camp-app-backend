package com.karmperis.campapp.dto;

import java.util.UUID;

/**
 * Data transfer object for reading a role.
 *
 * @param uuid the role's unique identifier
 * @param name the role's name
 */

public record RoleReadOnlyDTO(
        UUID uuid,
        String name
) {
}