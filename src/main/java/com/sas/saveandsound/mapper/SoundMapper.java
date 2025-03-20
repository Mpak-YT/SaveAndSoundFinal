package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SoundMapper {

    private SoundMapper() {}

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
        dto.setAlbum(sound.getAlbum());
        dto.setDate(sound.getDate());
        dto.setText(sound.getText());
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
        sound.setAlbum(dto.getAlbum());
        sound.setDate(dto.getDate());
        sound.setText(dto.getText());
        return sound;
    }

}
