package com.bci.reto.service.model;

import lombok.Value;

@Value
public class PhoneResponseDTO {
    Long number;
    Integer citycode;
    String contrycode;
}
