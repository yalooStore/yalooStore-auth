package com.yaloostore.auth.exception;

public class InvalidLoginRequestException extends RuntimeException {

    public InvalidLoginRequestException() {
        super("this is invalid login request");
    }
}
