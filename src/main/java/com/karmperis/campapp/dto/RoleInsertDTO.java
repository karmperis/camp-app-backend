package com.karmperis.campapp.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@GroupSequence({RoleInsertDTO.First.class, RoleInsertDTO.Second.class, RoleInsertDTO.class})
public record RoleInsertDTO(
        @NotBlank(message = "The role name cannot be blank.", groups = First.class)
        @Size(min = 4, max = 50, message = "The role name must contain between 4 and 50 characters.", groups = Second.class)
        String name
) {

    public interface First {
    }

    public interface Second {
    }
}