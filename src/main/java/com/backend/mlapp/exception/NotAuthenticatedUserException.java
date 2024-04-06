package com.backend.mlapp.exception;

public class NotAuthenticatedUserException extends RuntimeException {
    public NotAuthenticatedUserException(String message) {
        super(message);
    }
}
