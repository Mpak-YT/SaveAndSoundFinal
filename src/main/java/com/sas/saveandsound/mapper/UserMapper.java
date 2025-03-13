package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;

import java.util.stream.Collectors;

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
        dto.setSoundIds(user.getSounds().stream()
                .map(Sound::getId) // Только ID вместо полного объекта SoundDto
                .collect(Collectors.toSet()));
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        // Не заполняем sounds, так как они приходят только как ID (их можно подгружать в сервисе)
        return user;
    }
}
