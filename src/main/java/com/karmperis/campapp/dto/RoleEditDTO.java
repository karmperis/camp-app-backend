package com.karmperis.campapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object for editing a role.
 *
 * @param name the role name
 */

@GroupSequence({RoleEditDTO.First.class, RoleEditDTO.Second.class, RoleEditDTO.class})
public record RoleEditDTO(

        @NotBlank(message = "The role name cannot be blank.", groups = First.class)
        @Size(min = 4, max = 50, message = "The role name must contain between 4 and 50 characters.", groups = Second.class)
        String name
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