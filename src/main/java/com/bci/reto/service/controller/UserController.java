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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;


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
        return userService.signUp(request);
    }

    @Operation(summary = "Iniciar sesión", description = "Permite a un usuario autenticarse con un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/login")
    public Mono<ResponseEntity<UserResponseDTO>> login(@RequestHeader("Authorization") String authHeader) {
        return userService.login(authHeader);
    }
}