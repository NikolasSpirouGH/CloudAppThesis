package com.cloud_ml_app_thesis.exception;

import com.cloud_ml_app_thesis.dto.response.MyResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ✅ 1. Validation failed on request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MyResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        MyResponse<?> response = new MyResponse<>(null, "VALIDATION_ERROR", errorMessage, new Metadata());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ✅ 2. Validation failed on query/path parameters
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MyResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        MyResponse<?> response = new MyResponse<>(null, "CONSTRAINT_VIOLATION", errorMessage, new Metadata());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ✅ 3. Invalid/malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MyResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        MyResponse<?> response = new MyResponse<>(null, "MALFORMED_JSON", "Request body is invalid or malformed", new Metadata());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ✅ 4. Access denied (forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MyResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        MyResponse<?> response = new MyResponse<>(null, "ACCESS_DENIED", "You are not authorized to access this resource", new Metadata());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ✅ 5. Custom application-specific
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<MyResponse<?>> handleFileProcessingException(FileProcessingException ex) {
        MyResponse<?> errorResponse = new MyResponse<>(null, "FILE_ERROR", "File processing error", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MinioFileUploadException.class)
    public ResponseEntity<MyResponse<?>> handleMinioFileUploadException(MinioFileUploadException ex) {
        logger.error("Minio file upload exception: {}", ex.getMessage(), ex);
        MyResponse<?> errorResponse = new MyResponse<>(null, "MINIO_UPLOAD_ERROR", "File upload failed", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AlgorithmNotFoundException.class)
    public ResponseEntity<MyResponse<?>> handleAlgorithmNotFoundException(AlgorithmNotFoundException ex) {
        logger.error("Algorithm not found exception: {}", ex.getMessage(), ex);
        MyResponse<?> errorResponse = new MyResponse<>(null, "NOT_FOUND", "Algorithm could not be found", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ✅ 6. Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MyResponse<?>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        MyResponse<?> errorResponse = new MyResponse<>(
                null,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.",
                new Metadata()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MyResponse<?>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new MyResponse<>(null, "BAD_REQUEST", ex.getMessage(), new Metadata()));
    }

    @ExceptionHandler(MailSendingException.class)
    public ResponseEntity<MyResponse<?>> handleMailSendingException(MailSendingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MyResponse<>(null, "MAIL_SENDING_ERROR", ex.getMessage(), new Metadata()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<MyResponse<?>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MyResponse<>(null, "MISSING_REQUEST_HEADER_ERROR", ex.getMessage(), new Metadata()));
    }
}
