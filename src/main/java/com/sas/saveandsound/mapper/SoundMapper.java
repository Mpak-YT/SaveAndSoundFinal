package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.dto.AlbumNameDto;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.HashSet;

@Component
public class SoundMapper {

    private SoundMapper() {} // Private constructor for static utility class

    public static SoundDto toDto(Sound sound) {
        if (sound == null) return null;
        SoundDto dto = new SoundDto();
        dto.setId(sound.getId());
        dto.setName(sound.getName());
        dto.setText(sound.getText());
        dto.setDate(sound.getDate());
        if (sound.getCreators() != null) {
            dto.setCreators(sound.getCreators().stream()
                                 .map(UserMapper::toDto) // Use static method call
                                 .collect(Collectors.toSet()));
        } else {
            dto.setCreators(new HashSet<>());
        }
        if (sound.getAlbum() != null) {
            AlbumNameDto albumNameDto = new AlbumNameDto();
            albumNameDto.setId(sound.getAlbum().getId());
            albumNameDto.setName(sound.getAlbum().getName());
            dto.setAlbum(albumNameDto);
        }
        return dto;
    }

}
