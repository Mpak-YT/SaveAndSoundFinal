package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.CreatorDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private UserMapper() {}

    private static <T extends UserDto> T mapCommonFields(User user, T dto) {
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        return dto;
    }

    private static User mapCommonFields(UserDto dto, User user) {
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        return user;
    }

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return mapCommonFields(user, new UserDto());
    }

    public static CreatorDto toCreatorDto(User creator) {
        if (creator == null) return null;
        CreatorDto dto = mapCommonFields(creator, new CreatorDto());
        dto.setSounds(creator.getSounds().stream()
                .map(SoundMapper::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        return mapCommonFields(dto, new User());
    }

    public static User toEntity(CreatorDto dto) {
        if (dto == null) return null;
        User user = mapCommonFields(dto, new User());
        user.setSounds(dto.getSounds().stream()
                .map(SoundMapper::toEntity)
                .collect(Collectors.toSet()));
        return user;
    }
}
