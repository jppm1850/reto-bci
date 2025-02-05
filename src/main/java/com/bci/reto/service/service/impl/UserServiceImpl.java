package com.bci.reto.service.service.impl;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PhoneRepository phoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public Mono<UserResponseDTO> signUp(UserSignUpRequestDTO request) {
        return userRepository.findByEmail(request.email())
                .flatMap(existingUser -> Mono.<UserResponseDTO>error(
                        new UserExistsException("Email already registered")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = userMapper.toEntity(request);
                    user.setPassword(passwordEncoder.encode(request.password()));
                    user.setToken(jwtService.generateToken(user));

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                if (request.phones() == null || request.phones().isEmpty()) {
                                    return Mono.just(userMapper.toDTO(savedUser));
                                }

                                return phoneRepository.saveAll(request.phones().stream()
                                                .map(phoneDto -> {
                                                    Phone phone = userMapper.phoneRequestDTOToEntity(phoneDto);
                                                    phone.setUserId(savedUser.getId());
                                                    return phone;
                                                })
                                                .toList())
                                        .collectList()
                                        .map(savedPhones -> {
                                            savedUser.setPhones(savedPhones);
                                            return userMapper.toDTO(savedUser);
                                        });
                            });
                }));
    }


    public Mono<UserResponseDTO> login(String token) {
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
