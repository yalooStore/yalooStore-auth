package com.yaloostore.auth.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {

        super("Invalid Token exception");
    }
}
