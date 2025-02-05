package com.bci.reto.service.exception;

public final class UserExistsException extends RuntimeException implements UserError {
    public UserExistsException(String message) {
        super(message);
    }
}
