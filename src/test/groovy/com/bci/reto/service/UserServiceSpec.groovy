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
import com.bci.reto.service.service.impl.UserServiceImpl
import org.springframework.http.HttpStatus
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
    UserServiceImpl userService

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
        def phoneDto = new PhoneRequestDTO(123456789L, 1, "57")
        def request = new UserSignUpRequestDTO("Juan Pérez", "juan@ejemplo.com", "Contraseña123", [phoneDto])

        def user = new User()
        user.setName("Juan Pérez")
        user.setEmail("juan@ejemplo.com")

        def savedUser = new User()
        savedUser.setId(UUID.randomUUID())
        savedUser.setName("Juan Pérez")
        savedUser.setEmail("juan@ejemplo.com")
        savedUser.setCreated(LocalDateTime.now())
        savedUser.setLastLogin(LocalDateTime.now())
        savedUser.setIsActive(true)

        def phone = new Phone()
        phone.setNumber(123456789L)
        phone.setCitycode(1)
        phone.setContrycode("57")

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
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.empty()
        1 * userMapper.toEntity(request) >> user
        1 * passwordEncoder.encode(request.getPassword()) >> "contraseñaEncriptada"
        1 * jwtService.generateToken(_) >> token
        1 * userRepository.save(_) >> Mono.just(savedUser)
        1 * userMapper.phoneRequestDTOToEntity(_) >> phone
        1 * phoneRepository.saveAll(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO
        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería lanzar excepción cuando se intenta registrar con un email existente"() {
        given: 'una solicitud con email ya registrado'
        def request = new UserSignUpRequestDTO("Juan Pérez", "existente@ejemplo.com", "Contraseña123", [])
        def usuarioExistente = new User()
        usuarioExistente.setEmail(request.getEmail())

        when: 'se intenta registrar el usuario'
        def resultado = userService.signUp(request).block()

        then: 'debe devolver una respuesta de error'
        1 * userRepository.findByEmail(request.getEmail()) >> Mono.just(usuarioExistente)
        resultado.getStatusCode() == HttpStatus.BAD_REQUEST
        resultado.getBody().getError()[0].getDetail().contains("Email already registered")
    }

    def "debería iniciar sesión exitosamente"() {
        given: 'un token válido'
        def authHeader = "Bearer token.jwt.valido"
        def token = "token.jwt.valido"
        def email = "juan@ejemplo.com"

        def user = new User()
        user.setId(UUID.randomUUID())
        user.setName("Juan Pérez")
        user.setEmail(email)
        user.setCreated(LocalDateTime.now())
        user.setLastLogin(LocalDateTime.now())
        user.setIsActive(true)

        def phone = new Phone()
        phone.setNumber(123456789L)
        phone.setCitycode(1)
        phone.setContrycode("57")

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
        def resultado = userService.login(authHeader).block()

        then: 'debe procesar correctamente el inicio de sesión'
        1 * jwtService.validateTokenAndGetEmail(token) >> email
        1 * userRepository.findByEmail(email) >> Mono.just(user)
        1 * jwtService.generateToken(_) >> nuevoToken
        1 * userRepository.save(_) >> Mono.just(user)
        1 * phoneRepository.findByUserId(_) >> Flux.just(phone)
        1 * userMapper.toDTO(_) >> responseDTO
        resultado.getStatusCode() == HttpStatus.OK
        resultado.getBody() == responseDTO
    }

    def "debería retornar unauthorized cuando el header de autorización es inválido"() {
        given: 'un header de autorización inválido'
        def authHeader = "InvalidHeader"

        when: 'se intenta iniciar sesión'
        def resultado = userService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        0 * jwtService.validateTokenAndGetEmail(_)
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized cuando el token es inválido"() {
        given: 'un token inválido'
        def authHeader = "Bearer token.invalido"

        when: 'se intenta iniciar sesión'
        def resultado = userService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * jwtService.validateTokenAndGetEmail("token.invalido") >> { throw new RuntimeException("Token inválido") }
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }

    def "debería retornar unauthorized cuando el usuario no existe"() {
        given: 'un token válido pero usuario inexistente'
        def authHeader = "Bearer token.valido"
        def email = "noexiste@ejemplo.com"

        when: 'se intenta iniciar sesión'
        def resultado = userService.login(authHeader).block()

        then: 'debe retornar unauthorized'
        1 * jwtService.validateTokenAndGetEmail("token.valido") >> email
        1 * userRepository.findByEmail(email) >> Mono.empty()
        resultado.getStatusCode() == HttpStatus.UNAUTHORIZED
        resultado.getBody() == null
    }
}