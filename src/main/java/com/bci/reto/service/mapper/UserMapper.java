package com.bci.reto.service.mapper;

import com.bci.reto.service.model.PhoneRequestDTO;
import com.bci.reto.service.model.PhoneResponseDTO;
import com.bci.reto.service.model.UserResponseDTO;
import com.bci.reto.service.model.UserSignUpRequestDTO;
import com.bci.reto.service.entity.Phone;
import com.bci.reto.service.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(UserSignUpRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public Phone phoneRequestDTOToEntity(PhoneRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Phone phone = new Phone();
        phone.setNumber(dto.getNumber());
        phone.setCitycode(dto.getCitycode());
        phone.setContrycode(dto.getContrycode());
        return phone;
    }

    public UserResponseDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        List<PhoneResponseDTO> phoneDTOs = entity.getPhones().stream()
                .map(phone -> new PhoneResponseDTO(
                        phone.getNumber(),
                        phone.getCitycode(),
                        phone.getContrycode()
                ))
                .collect(Collectors.toList());

        return new UserResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                phoneDTOs,
                entity.getCreated(),
                entity.getLastLogin(),
                entity.getToken(),
                entity.getIsActive()
        );
    }
}