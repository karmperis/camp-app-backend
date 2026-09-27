package com.karmperis.campapp.core.exceptions;

/**
 * Exception thrown when an invalid argument is passed to an entity.
 */

public class EntityInvalidArgumentException extends AppGenericException {
    private static final String CODE_SUFFIX = "InvalidArgument";

    /**
     * Create a new exception for an "invalid argument" error.
     *
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityInvalidArgumentException(String code, String message) {
        super(code + CODE_SUFFIX, message);
    }
}