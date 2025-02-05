package com.bci.reto.service.config;

import com.bci.reto.service.model.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleValidationErrors(WebExchangeBindException ex) {

        log.error("Validation error: {}", ex.getMessage());
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO response = new ErrorResponseDTO(List.of(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.BAD_REQUEST.value(),
                        errors
                )
        ));

        return Mono.just(ResponseEntity.badRequest().body(response));
    }
}
