package com.yaloostore.auth.exception;

public class InvalidAuthorizationHeaderException extends RuntimeException {

    public InvalidAuthorizationHeaderException() {
        super("invalid authorization header");
    }
}
