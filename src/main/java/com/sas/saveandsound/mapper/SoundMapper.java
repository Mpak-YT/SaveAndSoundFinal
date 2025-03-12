package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;

import java.util.stream.Collectors;

public class SoundMapper {

    private SoundMapper() {
        throw
            new UnsupportedOperationException("It's a utility class and cannot be instantiated");
    }

    public static SoundDto toDto(Sound sound) {
        if (sound == null) {
            return null;
        }
        SoundDto dto = new SoundDto();
        dto.setId(sound.getId());
        dto.setName(sound.getName());
        dto.setCreators(sound.getCreators().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }

    public static Sound toEntity(SoundDto dto) {
        if (dto == null) {
            return null;
        }
        Sound sound = new Sound();
        sound.setName(dto.getName());
        sound.setCreators(dto.getCreators().stream()
                .map(UserMapper::toEntity)
                .collect(Collectors.toSet()));
        return sound;
    }
}
