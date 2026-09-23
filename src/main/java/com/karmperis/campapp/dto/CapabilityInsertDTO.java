package com.karmperis.campapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object for inserting a new capability.
 *
 * @param name        the capability name
 * @param description the capability description
 */

@GroupSequence({CapabilityInsertDTO.First.class, CapabilityInsertDTO.Second.class, CapabilityInsertDTO.class})
public record CapabilityInsertDTO(

        @NotBlank(message = "The capability name cannot be blank.", groups = First.class)
        @Size(min = 4, max = 100, message = "The capability name must contain between 4 and 100 characters.", groups = Second.class)
        String name,

        @Size(max = 255, message = "The capability description must contain maximum 255 characters.", groups = Second.class)
        String description
) {

    /**
     * Validation group for required field checks.
     */
    public interface First {
    }

    /**
     * Validation group for field length checks.
     */
    public interface Second {
    }
}