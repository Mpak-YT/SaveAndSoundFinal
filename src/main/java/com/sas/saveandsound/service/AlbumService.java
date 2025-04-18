package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.mapper.AlbumMapper;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.repository.AlbumRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlbumService {

    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);

    private final SoundService soundService;
    private final AlbumRepository albumRepository;

    private void logAlbumNotFound(long id) {
        logger.error("Album not found with ID: {}", id);
    }

    public AlbumService(SoundService soundService, AlbumRepository albumRepository) {
        this.soundService = soundService;
        this.albumRepository = albumRepository;
    }

    public List<AlbumNameDto> getAllAlbums() {
        logger.info("Fetching all albums...");
        List<Album> albums = albumRepository.findAll();
        if (albums.isEmpty()) {
            logger.warn("No albums found.");
            throw new AlbumNotFoundException("No albums found.");
        }
        logger.info("Found {} albums.", albums.size());
        return albums.stream()
                .map(AlbumMapper::toAlbumNameDto)
                .toList();
    }

    public AlbumDto search(long id) {
        logger.info("Fetching album with ID: {}", id);
        Album album = albumRepository.findById(id);
        if (album == null) {
            logAlbumNotFound(id);
            throw new AlbumNotFoundException("Album with ID " + id + " not found.");
        }
        logger.info("Album with ID {} retrieved successfully.", id);
        return AlbumMapper.toDto(album);
    }

    public List<AlbumDto> search(String name) {
        logger.info("Searching for albums with name: {}", name);
        List<Album> albums = albumRepository.findByName(name);
        if (albums == null || albums.isEmpty()) {
            logger.warn("No albums found with the name '{}'.", name);
            throw new AlbumNotFoundException("No albums found with the name '" + name + "'.");
        }
        logger.info("Found {} albums with the name '{}'.", albums.size(), name);
        return albums.stream()
                .map(AlbumMapper::toDto)
                .toList();
    }

    @Transactional
    public AlbumDto createAlbum(AlbumDto albumDto) {
        logger.info("Creating a new album with data: {}", albumDto);

        if (albumDto.getName() == null || albumDto.getName().trim().isEmpty()) {
            logger.error("Album name is null or empty.");
            throw new IllegalArgumentException("Album name cannot be null or empty.");
        }

        Album newAlbum = AlbumMapper.toEntity(albumDto);

        if (albumDto.getSounds() != null && !albumDto.getSounds().isEmpty()) {
            Set<Sound> sounds = albumDto.getSounds().stream()
                    .map(soundDto -> {
                        Sound sound = SoundMapper.toEntity(soundService.createSound(soundDto));
                        sound.setAlbum(newAlbum);
                        return sound;
                    })
                    .collect(Collectors.toSet());
            newAlbum.setSounds(sounds);
        }

        Album savedAlbum = albumRepository.save(newAlbum);
        logger.info("Album successfully created with ID: {}", savedAlbum.getId());
        return AlbumMapper.toDto(savedAlbum);
    }


    @Transactional
    public AlbumDto updateAlbum(long id, AlbumDto albumDto) {
        logger.info("Updating album with ID: {}", id);
        Album existingAlbum = albumRepository.findById(id);
        if (existingAlbum == null) {
            logAlbumNotFound(id);
            throw new AlbumNotFoundException("Unable to update album with ID " + id + ". Album not found.");
        }

        // Обновляем название, если оно не null
        if (albumDto.getName() != null) {
            existingAlbum.setName(albumDto.getName());
        }

        // Обновляем описание, если оно явно указано (включая null)
        existingAlbum.setDescription(albumDto.getDescription());

        if (albumDto.getSounds() != null && !albumDto.getSounds().isEmpty()) {
            Set<Sound> updatedSounds = albumDto.getSounds().stream()
                    .map(soundDto -> {
                        Sound sound;
                        if (soundDto.getId() != null) {
                            sound = SoundMapper.toEntity(soundService
                                    .updateSound(soundDto.getId(), soundDto));
                        } else {
                            sound = SoundMapper.toEntity(soundService.createSound(soundDto));
                        }
                        sound.setAlbum(existingAlbum);
                        return sound;
                    })
                    .collect(Collectors.toSet());
            existingAlbum.setSounds(updatedSounds);
        }

        Album updatedAlbum = albumRepository.save(existingAlbum);
        logger.info("Album with ID {} successfully updated.", id);
        return AlbumMapper.toDto(updatedAlbum);
    }


    @Transactional
    public void deleteAlbum(long id) {
        logger.info("Deleting album with ID: {}", id);
        Album album = albumRepository.findById(id);
        if (album == null) {
            logAlbumNotFound(id);
            throw new AlbumNotFoundException("Unable to delete album with ID: " + id + ". Album not found.");
        }
        albumRepository.delete(album);
        logger.info("Album with ID {} deleted successfully.", id);
    }

    @Transactional
    public void deleteAllAlbums() {
        logger.info("Deleting all albums...");
        albumRepository.deleteAll();
        logger.info("All albums deleted successfully.");
    }
}
