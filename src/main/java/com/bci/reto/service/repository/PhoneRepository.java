package com.bci.reto.service.repository;

import com.bci.reto.service.entity.Phone;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;


public interface PhoneRepository extends ReactiveCrudRepository<Phone, Long> {
    Flux<Phone> findByUserId(UUID userId);
}

