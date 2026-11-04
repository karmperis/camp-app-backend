package com.karmperis.campapp.core;

import com.karmperis.campapp.core.exceptions.*;
import com.karmperis.campapp.dto.ErrorResponseDTO;
import com.karmperis.campapp.dto.ValidationErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global REST exception handler that maps known exceptions to HTTP error responses.
 */

@RestControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link ValidationException} and returns a ResponseEntity with a ValidationErrorResponseDTO.
     * HTTP 400
     *
     * @param e the ValidationException to handle
     * @return response entity with the validation error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationException(ValidationException e) {
        log.warn("Validation Failed. Message={}", e.getMessage());

        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new ResponseEntity<>(new ValidationErrorResponseDTO(e.getCode(), e.getMessage(), errors),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link EntityInvalidArgumentException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 400
     *
     * @param e the EntityInvalidArgumentException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(EntityInvalidArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidArgumentException(EntityInvalidArgumentException e) {
        log.warn("Invalid Argument. Message={}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link EntityNotFoundException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 404
     *
     * @param e the EntityNotFoundException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found. Message={}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link EntityAlreadyExistsException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 409
     *
     * @param e the EntityAlreadyExistsException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        log.warn("Entity already exists. Message={}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link OperationNotAllowedException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 409
     *
     * @param e the OperationNotAllowedException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponseDTO> handleOperationNotAllowedException(OperationNotAllowedException e) {
        log.warn("Operation not allowed. Message={}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link AuthenticationException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 401
     *
     * @param e       the AuthenticationException to handle
     * @param request the HttpServletRequest
     * @return response entity with the error details
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {

        log.warn("Authentication failed for IP={}", request.getRemoteAddr());

        String errorCode = switch (e) {
            case BadCredentialsException ex -> "INVALID_CREDENTIALS";
            case DisabledException ex -> "ACCOUNT_DISABLED";
            case LockedException ex -> "ACCOUNT_LOCKED";
            case AccountExpiredException ex -> "ACCOUNT_EXPIRED";
            case CredentialsExpiredException ex -> "CREDENTIALS_EXPIRED";
            default -> "AUTHENTICATION_ERROR";
        };

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(errorCode, e.getMessage()));
    }

    /**
     * Handles {@link AccessDeniedException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 403
     *
     * @param e the AccessDeniedException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied. Message={}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO("ACCESS_DENIED", e.getMessage()));
    }

    /**
     * Handles {@link FileUploadException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 500
     *
     * @param e the FileUploadException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileUploadException(FileUploadException e) {
        log.error("File upload failed.", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link DataAccessException} and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 500
     *
     * @param e the DataAccessException to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseException(DataAccessException e) {
        log.error("Database operation failed.", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("DATABASE_ERROR", "A database error occurred."));
    }

    /**
     * Handles generic exceptions and returns a ResponseEntity with an ErrorResponseDTO.
     * HTTP 500
     *
     * @param e the Exception to handle
     * @return response entity with the error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("Unexpected error.", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("INTERNAL_SERVER_ERROR", "An unexpected error occurred."));
    }
}