package com.bci.reto.service.service;

import com.bci.reto.service.entity.User;

public interface JwtService {

    String generateToken(User user);
    String validateTokenAndGetEmail(String token);
}
