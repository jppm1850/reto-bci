package com.bci.reto.service.service;

import com.bci.reto.service.model.UserResponseDTO;
import com.bci.reto.service.model.UserSignUpRequestDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<ResponseEntity<Object>> signUp(UserSignUpRequestDTO request);
    Mono<ResponseEntity<UserResponseDTO>> login(String authHeader);
}
