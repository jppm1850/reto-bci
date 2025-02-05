package com.bci.reto.service.controller;

import com.bci.reto.service.model.ErrorResponseDTO;
import com.bci.reto.service.model.UserResponseDTO;
import com.bci.reto.service.model.UserSignUpRequestDTO;
import com.bci.reto.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Registra un nuevo usuario", description = "Permite registrar un usuario con validación de email y contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/sign-up")
    public Mono<ResponseEntity<Object>> signUp(@Valid @RequestBody UserSignUpRequestDTO request) {
        return userService.signUp(request)
                .map(response -> ResponseEntity.ok().body((Object)response))
                .onErrorResume(e -> {
                    log.error("Error in signup: ", e);
                    return Mono.just(ResponseEntity.badRequest()
                            .body(new ErrorResponseDTO(List.of(
                                    new ErrorResponseDTO.Error(
                                            new Timestamp(System.currentTimeMillis()),
                                            HttpStatus.BAD_REQUEST.value(),
                                            e.getMessage()
                                    )
                            ))));
                });
    }

    @Operation(summary = "Iniciar sesión", description = "Permite a un usuario autenticarse con un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/login")
    public Mono<ResponseEntity<UserResponseDTO>> login(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .<UserResponseDTO>status(HttpStatus.UNAUTHORIZED)
                    .body(null));
        }

        String token = authHeader.substring(7);
        return userService.login(token)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .<UserResponseDTO>status(HttpStatus.UNAUTHORIZED)
                        .body(null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleError(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponseDTO(Collections.singletonList(
                        new ErrorResponseDTO.Error(
                                new Timestamp(System.currentTimeMillis()),
                                HttpStatus.BAD_REQUEST.value(),
                                e.getMessage()
                        )
                )));
    }
}