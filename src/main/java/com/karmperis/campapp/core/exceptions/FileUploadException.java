package com.karmperis.campapp.core.exceptions;

/**
 * Exception thrown when a file upload fails.
 */

public class FileUploadException extends AppGenericException {
    private static final String CODE_SUFFIX = "FileUploadFailed";

    /**
     * Create a new exception for a "file upload" failure.
     *
     * @param code    base application error code/prefix
     * @param message readable error message
     */
    public FileUploadException(String code, String message) {
        super(code + CODE_SUFFIX, message);
    }
}