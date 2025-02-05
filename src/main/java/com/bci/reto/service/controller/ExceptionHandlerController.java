package com.bci.reto.service.controller;

import com.bci.reto.service.model.ErrorResponseDTO;
import com.bci.reto.service.exception.UserExistsException;
import com.bci.reto.service.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.stream.Collectors;
import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(WebExchangeBindException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.BAD_REQUEST.value(),
                                message
                        )
                )));
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserExists(UserExistsException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.BAD_REQUEST.value(),
                                ex.getMessage()
                        )
                )));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage()
                        )
                )));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleJwtException(JwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Invalid or expired token"
                        )
                )));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "An unexpected error occurred"
                        )
                )));
    }
}
