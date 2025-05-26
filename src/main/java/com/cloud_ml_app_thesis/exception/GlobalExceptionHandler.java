package com.cloud_ml_app_thesis.exception;

import com.cloud_ml_app_thesis.dto.response.GenericResponse;
import com.cloud_ml_app_thesis.dto.response.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<GenericResponse<?>> handleGenericException(Exception ex, WebRequest request) {
        GenericResponse<?> errorResponse = new GenericResponse<>(null, null, "Internal Server Error",new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<GenericResponse<?>> handleFileProcessingException(FileProcessingException ex) {
        GenericResponse<?> errorResponse = new GenericResponse<>(null, null,"File processing error", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MinioFileUploadException.class)
    public ResponseEntity<GenericResponse<?>> handleMinioFileUploadException(MinioFileUploadException ex) {
        logger.error("Minio file upload exception: {}", ex.getMessage(), ex);
        GenericResponse<?> errorResponse = new GenericResponse<>(null, null, "File upload failed", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AlgorithmNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleAlgorithmNotFoundException(AlgorithmNotFoundException ex) {
        logger.error("Algorithm not found exception: {}", ex.getMessage(), ex);
        GenericResponse errorResponse = new GenericResponse(null, null, "Algorithm could not be found", new Metadata());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Removed the second generic exception handler to avoid ambiguity
    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("An error occurred", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    */
}
