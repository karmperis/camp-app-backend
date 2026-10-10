package com.karmperis.campapp.core.exceptions;

/**
 * Exception thrown when an entity already exists.
 */

public class EntityAlreadyExistsException extends AppGenericException {
    private static final String CODE_SUFFIX = "AlreadyExists";

    /**
     * Creates a new exception for an "already exists" error.
     *
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public EntityAlreadyExistsException(String code, String message) {
        super(code + CODE_SUFFIX, message);
    }
}