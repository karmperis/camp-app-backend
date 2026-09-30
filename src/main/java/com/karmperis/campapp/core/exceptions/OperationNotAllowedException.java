package com.karmperis.campapp.core.exceptions;

/**
 * Exception thrown when a business rule prevents an operation.
 */

public class OperationNotAllowedException extends AppGenericException {
    private static final String CODE_SUFFIX = "OperationNotAllowed";

    /**
     * Creates a new exception for an "operation not allowed" error.
     *
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public OperationNotAllowedException(String code, String message) {
        super(code + CODE_SUFFIX, message);
    }
}