package com.bci.reto.service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un teléfono asociado a un usuario")
public record PhoneRequestDTO(
        @Schema(description = "Número de teléfono", example = "123456789") Long number,
        @Schema(description = "Código de ciudad", example = "1") Integer citycode,
        @Schema(description = "Código de país", example = "57") String contrycode
) {}
