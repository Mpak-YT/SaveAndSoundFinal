package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.model.Album;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.HashSet;

@Component
public class AlbumMapper {

    private AlbumMapper() {} // Private constructor for static utility class

    public static AlbumDto toDto(Album album) {
        if (album == null) {
            return null;
        }
        AlbumDto dto = new AlbumDto();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setDescription(album.getDescription());
        if (album.getSounds() != null) {
            dto.setSounds(album.getSounds().stream()
                               .map(SoundMapper::toDto) // Use static method call
                               .collect(Collectors.toSet()));
        } else {
            dto.setSounds(new HashSet<>()); // Ensure sounds collection is never null in DTO
        }
        return dto;
    }

    public static Album toEntity(AlbumDto dto) {
        if (dto == null) {
            return null;
        }
        Album album = new Album();
        album.setId(dto.getId());
        album.setName(dto.getName());
        album.setDescription(dto.getDescription());
        // Sounds will be handled in AlbumService to fetch actual Sound entities
        return album;
    }
}
