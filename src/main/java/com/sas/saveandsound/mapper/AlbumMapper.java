package com.sas.saveandsound.mapper;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.model.Album;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AlbumMapper {

    private AlbumMapper() {}

    public static AlbumDto toDto(Album album) {
        if (album == null) {
            return null;
        }
        AlbumDto dto = new AlbumDto();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setSounds(album.getSounds().stream()
                .map(SoundMapper::toDto)
                .collect(Collectors.toSet()));
        dto.setDescription(album.getDescription());
        return dto;
    }

    public static AlbumNameDto toAlbumNameDto(Album album) {
        if (album == null) {
            return null;
        }
        AlbumNameDto dto = new AlbumNameDto();
        dto.setId(album.getId());
        dto.setName(album.getName());
        return dto;
    }

    public static Album toEntity(AlbumNameDto dto) {
        if (dto == null) {
            return null;
        }
        Album album = new Album();
        album.setName(dto.getName());
        return album;
    }

    public static Album toEntity(AlbumDto dto) {
        if (dto == null) {
            return null;
        }
        Album album = new Album();
        album.setName(dto.getName());
        album.setSounds(dto.getSounds().stream()
                .map(SoundMapper::toEntity)
                .collect(Collectors.toSet()));
        album.setDescription(dto.getDescription());
        return album;
    }

}
