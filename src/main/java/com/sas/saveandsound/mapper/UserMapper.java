package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.CreatorDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private UserMapper() {}

    public static UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        return dto;
    }

    // Проверьте, что метод toCreatorDto(User user) возвращает CreatorDto,
    // где поле sounds заполняется из user.getSounds()
    public static CreatorDto toCreatorDto(User user) {
        if (user == null) return null;
        CreatorDto dto = new CreatorDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        // Корректно маппим список песен
        if (user.getSounds() != null) {
            Set<SoundDto> soundDtos = user.getSounds().stream()
                .map(SoundMapper::toDto)
                .collect(Collectors.toSet());
            dto.setSounds(soundDtos);
        } else {
            dto.setSounds(new java.util.HashSet<>());
        }
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        return user;
    }
}
