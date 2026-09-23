package com.karmperis.campapp.dto;

import jakarta.validation.constraints.Size;

/**
 * Data transfer object for editing a capability.
 *
 * @param description the capability description
 */

public record CapabilityEditDTO(

        @Size(max = 255, message = "The capability description must contain maximum 255 characters.")
        String description
) {
}