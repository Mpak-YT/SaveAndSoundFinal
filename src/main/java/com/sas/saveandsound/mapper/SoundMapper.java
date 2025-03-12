package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;

public class SoundMapper {

    // Приватный конструктор, чтобы предотвратить создание экземпляров
    private SoundMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Маппинг из Sound в SoundDto
    public static SoundDto toDto(Sound sound) {
        if (sound == null) {
            return null;
        }

        SoundDto dto = new SoundDto();
        dto.setId(sound.getId());
        dto.setName(sound.getName());
        dto.setCreators(sound.getCreators()); // Здесь копируется ссылка на Set<User>
        return dto;
    }

    // Маппинг из SoundDto в Sound
    public static Sound toEntity(SoundDto dto) {
        if (dto == null) {
            return null;
        }

        Sound sound = new Sound();
        sound.setName(dto.getName());
        sound.setCreators(dto.getCreators()); // Здесь копируется ссылка на Set<User>
        return sound;
    }
}
