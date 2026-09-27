package com.karmperis.campapp.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * Exception thrown when validation fails.
 */

@Getter
public class ValidationException extends AppGenericException {
    private static final String CODE_SUFFIX = "ValidationError";
    private final BindingResult bindingResult;

    /**
     * Creates a new exception for a "validation" failure.
     *
     * @param code          base application error code/prefix
     * @param message       readable error message
     * @param bindingResult the binding result containing the validation errors
     */
    public ValidationException(String code, String message, BindingResult bindingResult) {
        super(code + CODE_SUFFIX, message);
        this.bindingResult = bindingResult;
    }
}
