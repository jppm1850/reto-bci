package com.bci.reto.service.exception;

public sealed interface UserError
        permits UserExistsException, UserNotFoundException {
    String getMessage();
}
