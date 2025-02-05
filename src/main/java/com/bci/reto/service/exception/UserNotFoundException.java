package com.bci.reto.service.exception;


public final class UserNotFoundException extends RuntimeException implements UserError {
    public UserNotFoundException(String message) {
        super(message);
    }
}
