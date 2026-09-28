package com.karmperis.campapp.core.exceptions;

/**
 * Exception thrown when an entity is not found.
 */

public class EntityNotFoundException extends AppGenericException {
    private static final String CODE_SUFFIX = "NotFound";

    /**
     * Create a new exception for a "not found" error.
     *
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityNotFoundException(String code, String message) {
        super(code + CODE_SUFFIX, message);
    }
}