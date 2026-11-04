package com.karmperis.campapp.core.exceptions;

import lombok.Getter;

/**
 * Generic application exception that carries an application-specific error code.
 */

@Getter
public class AppGenericException extends Exception {
    private final String code;

    /**
     * Creates a new exception with the given error code and message.
     *
     * @param code    application-specific error code
     * @param message readable error message
     */
    public AppGenericException(String code, String message) {
        super(message);
        this.code = code;
    }
}