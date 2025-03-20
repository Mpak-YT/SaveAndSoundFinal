package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private UserMapper() {}

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setSounds(user.getSounds().stream()
                .map(SoundMapper::toDto) // Только ID вместо полного объекта SoundDto
                .collect(Collectors.toSet()));
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        // Не заполняем sounds, так как они приходят только как ID (их можно подгружать в сервисе)
        return user;
    }
}
