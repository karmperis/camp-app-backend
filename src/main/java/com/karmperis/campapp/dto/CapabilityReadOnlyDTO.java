package com.karmperis.campapp.dto;

import java.util.UUID;

/**
 * Data transfer object for reading a capability.
 *
 * @param uuid        the capability unique identifier
 * @param name        the capability name
 * @param description the capability description
 */

public record CapabilityReadOnlyDTO(
        UUID uuid,
        String name,
        String description
) {
}