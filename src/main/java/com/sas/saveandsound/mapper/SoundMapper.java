package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SoundMapper {

    private final UserRepository userRepository;

    public SoundMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SoundDto toDto(Sound sound) {
        if (sound == null) {
            return null;
        }
        SoundDto dto = new SoundDto();
        dto.setId(sound.getId());
        dto.setName(sound.getName());
        dto.setCreatorIds(sound.getCreators().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
        return dto;
    }

    public Sound toEntity(SoundDto dto) {
        if (dto == null) {
            return null;
        }

        Sound sound = new Sound();
        sound.setName(dto.getName());

        if (!dto.getCreatorIds().isEmpty()) {
            Set<User> creators = dto.getCreatorIds().stream()
                    .map(userRepository::findById) // Убираем лишнюю лямбду
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            sound.setCreators(creators);
        }

        return sound;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
