package com.karmperis.campapp.dto;

import java.util.Map;

/**
 * A data transfer object used for validation error responses.
 *
 * @param code    the error code
 * @param message the error message
 * @param errors  the validation messages keyed by field name
 */

public record ValidationErrorResponseDTO(String code, String message, Map<String, String> errors) {
}