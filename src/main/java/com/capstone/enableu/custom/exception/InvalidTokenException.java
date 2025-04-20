package com.capstone.enableu.custom.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException (String message) {
        super(message);
    }
}
