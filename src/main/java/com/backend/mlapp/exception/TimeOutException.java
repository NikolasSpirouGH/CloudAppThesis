package com.backend.mlapp.exception;

public class TimeOutException extends RuntimeException{
    public TimeOutException(String message){
        super(message);
    }
}
