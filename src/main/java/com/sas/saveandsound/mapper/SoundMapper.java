package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SoundMapper {

    private final UserRepository userRepository;

    public SoundMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static SoundDto toDto(Sound sound) {
        if (sound == null) {
            return null;
        }
        SoundDto dto = new SoundDto();
        dto.setId(sound.getId());
        dto.setName(sound.getName());
        dto.setCreators(sound.getCreators());
        dto.setAlbum(sound.getAlbum());
        dto.setText(sound.getText());
        dto.setCreationDate(sound.getDate());
        return dto;
    }

    public static Sound toEntity(SoundDto dto) {
        if (dto == null) {
            return null;
        }

        Sound sound = new Sound();
        sound.setName(dto.getName());

        if (!dto.getCreators().isEmpty()) {
            Set<User> creators = dto.getCreators();
            sound.setCreators(creators);
            sound.setAlbum(dto.getAlbum());
            sound.setText(dto.getText());
            sound.setDate(dto.getCreationDate());
        }

        return sound;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
