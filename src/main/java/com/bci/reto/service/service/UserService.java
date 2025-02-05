package com.bci.reto.service.service;

import com.bci.reto.service.model.UserResponseDTO;
import com.bci.reto.service.model.UserSignUpRequestDTO;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserResponseDTO> signUp(UserSignUpRequestDTO request);
    Mono<UserResponseDTO> login(String token);
}
