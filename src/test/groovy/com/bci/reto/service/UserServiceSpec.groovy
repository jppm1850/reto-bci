package com.bci.reto.service

import com.bci.reto.service.entity.Phone
import com.bci.reto.service.entity.User
import com.bci.reto.service.exception.UserExistsException
import com.bci.reto.service.exception.UserNotFoundException
import com.bci.reto.service.mapper.UserMapper
import com.bci.reto.service.model.PhoneRequestDTO
import com.bci.reto.service.model.PhoneResponseDTO
import com.bci.reto.service.model.UserResponseDTO
import com.bci.reto.service.model.UserSignUpRequestDTO
import com.bci.reto.service.repository.PhoneRepository
import com.bci.reto.service.repository.UserRepository
import com.bci.reto.service.service.JwtService
import com.bci.reto.service.service.UserService
import com.bci.reto.service.service.impl.UserServiceImpl
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.LocalDateTime

class UserServiceSpec extends Specification {
    UserRepository userRepository
    PhoneRepository phoneRepository
    PasswordEncoder passwordEncoder
    JwtService jwtService
    UserMapper userMapper
    UserService userService

    def setup() {
        userRepository = Mock()
        phoneRepository = Mock()
        passwordEncoder = Mock()
        jwtService = Mock()
        userMapper = Mock()
        userService = new UserServiceImpl(userRepository, phoneRepository, passwordEncoder, jwtService, userMapper)
    }

    def "debería registrar un nuevo usuario exitosamente"() {
        given: 'una solicitud de registro válida'
        def request = new UserSignUpRequestDTO(
                "Juan Pérez",
                "juan@ejemplo.com",
                "Contraseña123",
                [new PhoneRequestDTO(123456789L, 1, "57")]
        )
        def user = new User(
                name: request.name(),
                email: request.email()
        )
        def savedUser = new User(
                id: UUID.randomUUID(),
                name: request.name(),
                email: request.email(),
                created: LocalDateTime.now(),
                lastLogin: LocalDateTime.now(),
                isActive: true
        )
        def phone = new Phone(
                number: 123456789L,
                citycode: 1,
                contrycode: "57"
        )
        def token = "jwt.token"
        def responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                [new PhoneResponseDTO(phone.getNumber(), phone.getCitycode(), phone.getContrycode())],
                savedUser.getCreated(),
                savedUser.getLastLogin(),
                token,
                savedUser.getIsActive()
        )

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'se debe procesar correctamente la solicitud'
        1 * userRepository.findByEmail(request.email()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> user
        1 * passwordEncoder.encode(request.password()) >> "contraseñaEncriptada"
        1 * jwtService.generateToken(_) >> token
        1 * userRepository.save(_) >> Mono.just(savedUser)
        1 * userMapper.phoneRequestDTOToEntity(_) >> phone
        1 * phoneRepository.saveAll(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO
        resultado == responseDTO
    }

    def "debería lanzar excepción cuando se intenta registrar con un email existente"() {
        given: 'una solicitud con email ya registrado'
        def request = new UserSignUpRequestDTO(
                "Juan Pérez",
                "existente@ejemplo.com",
                "Contraseña123",
                []
        )
        def usuarioExistente = new User(email: request.email())

        when: 'se intenta registrar el usuario'
        userService.signUp(request).block()

        then: 'debe lanzar una excepción de usuario existente'
        1 * userRepository.findByEmail(request.email()) >> Mono.just(usuarioExistente)
        thrown(UserExistsException)
    }

    def "debería iniciar sesión exitosamente"() {
        given: 'un token válido'
        def token = "token.jwt.valido"
        def email = "juan@ejemplo.com"
        def user = new User(
                id: UUID.randomUUID(),
                name: "Juan Pérez",
                email: email,
                created: LocalDateTime.now(),
                lastLogin: LocalDateTime.now(),
                isActive: true
        )
        def phone = new Phone(
                number: 123456789L,
                citycode: 1,
                contrycode: "57"
        )
        def nuevoToken = "nuevo.token.jwt"
        def responseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                [new PhoneResponseDTO(phone.getNumber(), phone.getCitycode(), phone.getContrycode())],
                user.getCreated(),
                user.getLastLogin(),
                nuevoToken,
                user.getIsActive()
        )

        when: 'se intenta iniciar sesión'
        def resultado = userService.login(token).block()

        then: 'debe procesar correctamente el inicio de sesión'
        1 * jwtService.validateTokenAndGetEmail(token) >> email
        1 * userRepository.findByEmail(email) >> Mono.just(user)
        1 * jwtService.generateToken(_) >> nuevoToken
        1 * userRepository.save(_) >> Mono.just(user)
        1 * phoneRepository.findByUserId(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO
        resultado == responseDTO
    }

    def "debería lanzar excepción cuando el token es inválido"() {
        given: 'un token inválido'
        def token = "token.invalido"

        when: 'se intenta iniciar sesión'
        userService.login(token).block()

        then: 'debe lanzar una excepción'
        1 * jwtService.validateTokenAndGetEmail(token) >> { throw new RuntimeException("Token inválido") }
        thrown(RuntimeException)
    }

    def "debería lanzar excepción cuando el usuario no existe al iniciar sesión"() {
        given: 'un token válido pero usuario inexistente'
        def token = "token.valido"
        def email = "noexiste@ejemplo.com"

        when: 'se intenta iniciar sesión'
        userService.login(token).block()

        then: 'debe lanzar una excepción de usuario no encontrado'
        1 * jwtService.validateTokenAndGetEmail(token) >> email
        1 * userRepository.findByEmail(email) >> Mono.empty()
        thrown(UserNotFoundException)
    }
}