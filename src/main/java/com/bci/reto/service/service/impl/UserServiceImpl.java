package com.bci.reto.service.service.impl;

import com.bci.reto.service.model.ErrorResponseDTO;
import com.bci.reto.service.model.UserResponseDTO;
import com.bci.reto.service.model.UserSignUpRequestDTO;
import com.bci.reto.service.entity.Phone;
import com.bci.reto.service.entity.User;
import com.bci.reto.service.exception.UserExistsException;
import com.bci.reto.service.exception.UserNotFoundException;
import com.bci.reto.service.mapper.UserMapper;
import com.bci.reto.service.repository.PhoneRepository;
import com.bci.reto.service.repository.UserRepository;
import com.bci.reto.service.service.JwtService;
import com.bci.reto.service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public Mono<ResponseEntity<Object>> signUp(UserSignUpRequestDTO request) {
        return processSignUp(request)
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

    private Mono<UserResponseDTO> processSignUp(UserSignUpRequestDTO request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(existingUser -> Mono.<UserResponseDTO>error(
                        new UserExistsException("Email already registered")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = userMapper.toEntity(request);
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setToken(jwtService.generateToken(user));

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                if (request.getPhones() == null || request.getPhones().isEmpty()) {
                                    return Mono.just(userMapper.toDTO(savedUser));
                                }

                                return phoneRepository.saveAll(request.getPhones().stream()
                                                .map(phoneDto -> {
                                                    Phone phone = userMapper.phoneRequestDTOToEntity(phoneDto);
                                                    phone.setUserId(savedUser.getId());
                                                    return phone;
                                                })
                                                .collect(Collectors.toList()))
                                        .collectList()
                                        .map(savedPhones -> {
                                            savedUser.setPhones(savedPhones);
                                            return userMapper.toDTO(savedUser);
                                        });
                            });
                }));
    }

    @Override
    public Mono<ResponseEntity<UserResponseDTO>> login(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null));
        }

        String token = authHeader.substring(7);
        return processLogin(token)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(null)));
    }

    private Mono<UserResponseDTO> processLogin(String token) {
        return Mono.fromCallable(() -> jwtService.validateTokenAndGetEmail(token))
                .flatMap(email -> userRepository.findByEmail(email))
                .flatMap(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    user.setToken(jwtService.generateToken(user));

                    return userRepository.save(user)
                            .flatMap(savedUser -> phoneRepository.findByUserId(savedUser.getId())
                                    .collectList()
                                    .map(phones -> {
                                        savedUser.setPhones(phones);
                                        return userMapper.toDTO(savedUser);
                                    })
                            );
                })
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")));
    }
}