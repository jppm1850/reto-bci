package com.bci.reto.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Schema(description = "Datos para el registro de usuario")
public record UserSignUpRequestDTO(
        @Schema(description = "Nombre del usuario", example = "Juan Pérez")
        String name,

        @Email(message = "El formato del correo electrónico no es válido")
        @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
        String email,

        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d.*\\d)(?=.*[a-z])[A-Za-z\\d]{8,12}$",
                message = "La contraseña debe tener una letra mayúscula, exactamente dos números y tener entre 8 y 12 caracteres"
        )
        @Schema(description = "Contraseña del usuario (Debe tener al menos una mayúscula y exactamente dos números)",
                example = "Ab123456")
        String password,

        @Schema(description = "Lista de teléfonos asociados al usuario")
        List<PhoneRequestDTO> phones
) {}
