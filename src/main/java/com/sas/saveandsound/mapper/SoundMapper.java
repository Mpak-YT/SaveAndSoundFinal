package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class SoundMapper {

    // Приватный конструктор для предотвращения создания экземпляра
    private SoundMapper() {}

    // Преобразование из сущности Sound в DTO
    public static SoundDto toDto(Sound sound) {
        if (sound == null) {
            return null; // Возвращаем null, если объект Sound отсутствует
        }
        SoundDto dto = new SoundDto();
        dto.setId(sound.getId()); // Устанавливаем ID
        dto.setName(sound.getName()); // Устанавливаем имя

        // Безопасная обработка поля Creators (null -> пустой набор)
        dto.setCreators(sound.getCreators() == null
                ? null // Устанавливаем null, если creators отсутствуют
                : sound.getCreators().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toSet()));


        // Безопасная обработка поля Album
        dto.setAlbum(sound.getAlbum() == null ? null : AlbumMapper.toAlbumNameDto(sound.getAlbum()));

        // Устанавливаем остальные поля
        dto.setDate(sound.getDate());
        dto.setText(sound.getText());

        return dto;
    }

    // Преобразование из DTO в сущность Sound
    public static Sound toEntity(SoundDto dto) {
        if (dto == null) {
            return null; // Возвращаем null, если объект SoundDto отсутствует
        }
        Sound sound = new Sound();
        sound.setName(dto.getName()); // Устанавливаем имя

        // Безопасная обработка поля Creators (null -> пустой набор)
        sound.setCreators(dto.getCreators() == null
                ? Collections.emptySet()
                : dto.getCreators().stream()
                .map(UserMapper::toEntity)
                .collect(Collectors.toSet()));

        // Безопасная обработка поля Album
        sound.setAlbum(dto.getAlbum() == null ? null : AlbumMapper.toEntity(dto.getAlbum()));

        // Устанавливаем остальные поля
        sound.setDate(dto.getDate());
        sound.setText(dto.getText());

        return sound;
    }
}
