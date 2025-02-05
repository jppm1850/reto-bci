package com.bci.reto.service

import com.bci.reto.service.entity.User
import com.bci.reto.service.service.impl.JwtServiceImpl
import io.jsonwebtoken.JwtException
import spock.lang.Specification

class JwtServiceSpec extends Specification {
    JwtServiceImpl jwtService

    def setup() {
        jwtService = new JwtServiceImpl("4qhq8LrEBfYcaRHxhdb9zURb2rf8e7Ud8GLO9L6brain2rvUKu7C")
    }

    def "should generate valid JWT token"() {
        given:
        def user = new User()
        user.setEmail("test@example.com")

        when:
        def token = jwtService.generateToken(user)

        then:
        token != null
        token.split("\\.").length == 3 // Header.Payload.Signature
    }

    def "should validate token and return email"() {
        given:
        def user = new User()
        user.setEmail("test@example.com")
        def token = jwtService.generateToken(user)

        when:
        def email = jwtService.validateTokenAndGetEmail(token)

        then:
        email == user.getEmail()
    }

    def "should throw exception for invalid token"() {
        given:
        def invalidToken = "invalid.token"

        when:
        jwtService.validateTokenAndGetEmail(invalidToken)

        then:
        thrown(JwtException)
    }
}
