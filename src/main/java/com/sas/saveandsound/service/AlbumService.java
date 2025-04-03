package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.mapper.AlbumMapper;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.repository.AlbumRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AlbumService {

    private final SoundService soundService;
    private final AlbumRepository albumRepository;

    public AlbumService(SoundService soundService, AlbumRepository albumRepository) {
        this.soundService = soundService;
        this.albumRepository = albumRepository;
    }

    public List<AlbumNameDto> getAllAlbums() {
        return albumRepository.findAll()
                .stream()
                .map(AlbumMapper::toAlbumNameDto)
                .toList();
    }

    public AlbumDto search(long id) {
        Album album = albumRepository.findById(id);
        return album == null ? null : AlbumMapper.toDto(album);
    }

    public List<AlbumDto> search(String name) {
        List<Album> albums = albumRepository.findByName(name);
        return albums == null ? null : albums.stream().map(AlbumMapper::toDto).toList();
    }

    @Transactional
    public AlbumDto createAlbum(Map<String, Object> albumData) {
        Album newAlbum = mapAlbumFields(new Album(), albumData);
        return AlbumMapper.toDto(albumRepository.save(newAlbum));
    }

    @Transactional
    public AlbumDto updateAlbum(long id, Map<String, Object> albumData) {
        Album existingAlbum = albumRepository.findById(id);
        if (existingAlbum == null) {
            throw new EntityNotFoundException("Album not found");
        }
        mapAlbumFields(existingAlbum, albumData);
        Album updatedAlbum = albumRepository.save(existingAlbum);
        return AlbumMapper.toDto(updatedAlbum);
    }

    @Transactional
    public void deleteAlbum(long id) {
        Album album = albumRepository.findById(id);
        if (album == null) {
            throw new AlbumNotFoundException("Unable to delete album with ID " + id + ". Album not found.");
        }
        albumRepository.deleteById(id); // Удалить альбом
    }


    // Вынесенный метод для заполнения/обновления полей альбома
    private Album mapAlbumFields(Album album, Map<String, Object> albumData) {
        if (albumData.containsKey("name")) {
            album.setName((String) albumData.get("name"));
        }
        if (albumData.containsKey("description")) {
            album.setDescription((String) albumData.get("description"));
        }

        if (albumData.containsKey("sounds")) {
            // Получаем новые звуки
            Object soundsObject = albumData.get("sounds");

            List<Map<String, Object>> soundsData;
            if (soundsObject instanceof List) {
                soundsData = (List<Map<String, Object>>) soundsObject;
            } else if (soundsObject instanceof Map) {
                soundsData = List.of((Map<String, Object>) soundsObject);
            } else {
                throw new IllegalArgumentException("Invalid sounds format");
            }

            // Создаем новый набор звуков, который будет привязан к альбому
            Set<Sound> updatedSounds = soundsData.stream()
                    .map(data -> {
                        Sound sound;
                        if (data.containsKey("id")) {
                            Long soundId = ((Number) data.get("id")).longValue();
                            sound = SoundMapper.toEntity(soundService.updateSound(soundId, data));
                        } else {
                            sound = SoundMapper.toEntity(soundService.createSound(data));
                        }
                        sound.setAlbum(album); // Привязываем к альбому
                        return sound;
                    })
                    .collect(Collectors.toSet());

            // Убедитесь, что звуки, которые больше не нужны, удаляются
            Set<Sound> currentSounds = album.getSounds();

            // Сначала удалим из текущей коллекции те звуки, которые больше не присутствуют
            currentSounds.removeIf(existingSound -> !updatedSounds.contains(existingSound));

            // Обновляем коллекцию звуков в альбоме
            album.setSounds(updatedSounds);

            // Здесь Hibernate сам обработает удаление сирот, так как orphanRemoval = true
        }

        return album;
    }



    @Transactional
    public void deleteAllAlbums() {
        albumRepository.deleteAll();
    }
}
