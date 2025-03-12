package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.model.User;

public class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    // Маппинг из User в UserDto
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setSounds(user.getSounds()); // Здесь копируется ссылка на Set<User>
        return dto;
    }

    // Маппинг из UserDto в User
    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.getName());
        user.setSounds(dto.getSounds()); // Здесь копируется ссылка на Set<User>
        return user;
    }
}
