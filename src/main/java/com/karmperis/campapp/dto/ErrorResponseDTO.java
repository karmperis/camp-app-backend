package com.karmperis.campapp.dto;

/**
 * A data transfer object used to return a standardized error response.
 *
 * @param code    application-specific error code
 * @param message readable error message
 */
public record ErrorResponseDTO(String code, String message) {

    /**
     * Creates an error response with an empty message.
     *
     * @param code application-specific error code
     */
    public ErrorResponseDTO(String code) {
        this(code, "");
    }
}