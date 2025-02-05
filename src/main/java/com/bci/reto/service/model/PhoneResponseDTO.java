package com.bci.reto.service.model;

public record PhoneResponseDTO(
        Long number,
        Integer citycode,
        String contrycode
) {}
