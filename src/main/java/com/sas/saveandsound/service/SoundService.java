package com.sas.saveandsound.service;

import com.sas.saveandsound.cashe.SoundCache;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.AlbumRepository;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final SoundCache soundCache; // Используем SoundCache вместо Map
    private static final Logger logger = LoggerFactory.getLogger(SoundService.class);

    public SoundService(SoundRepository soundRepository,
                        AlbumRepository albumRepository,
                        UserRepository userRepository,
                        SoundCache soundCache) {
        this.soundRepository = soundRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.soundCache = soundCache;
    }

    public List<SoundDto> getAllSounds() {
        return soundRepository.findAll()
                .stream()
                .map(SoundMapper::toDto)
                .toList();
    }

    public SoundDto search(long id) {
        return SoundMapper.toDto(soundRepository.findById(id));
    }

    public List<SoundDto> search(String name) {
        return soundRepository.findByName(name)
                .stream()
                .map(SoundMapper::toDto)
                .toList();
    }

    @Transactional
    public SoundDto createSound(Map<String, Object> soundData) {
        Sound newSound = mapSoundFields(new Sound(), soundData);

        // Сохранение альбома, если он не сохранён
        if (newSound.getAlbum() != null && newSound.getAlbum().getId() == null) {
            Album savedAlbum = albumRepository.save(newSound.getAlbum());
            newSound.setAlbum(savedAlbum);
        }

        // Сохранение звука
        Sound savedSound = soundRepository.save(newSound);
        return SoundMapper.toDto(savedSound);
    }

    @Transactional
    public SoundDto updateSound(long id, Map<String, Object> soundData) {
        Sound existingSound = soundRepository.findById(id);

        if (existingSound == null) {
            throw new EntityNotFoundException("Sound not found");
        }

        // Обновление полей звука
        Sound existSound = mapSoundFields(existingSound, soundData);

        // Сохранение альбома, если он не сохранён
        if (existSound.getAlbum() != null && existSound.getAlbum().getId() == null) {
            Album savedAlbum = albumRepository.save(existSound.getAlbum());
            existSound.setAlbum(savedAlbum);
        }

        // Сохранение обновлённого звука
        Sound updatedSound = soundRepository.save(existSound);
        return SoundMapper.toDto(updatedSound);
    }

    public void deleteSound(long soundId) {
        // Проверяем существование звука
        Sound sound = soundRepository.findById(soundId);
        if (sound == null) {
            throw new SoundNotFoundException("Unable to delete sound with ID "
                    + soundId + ". Sound not found.");
        }

        // Удаление звука из базы данных
        soundRepository.delete(sound);

        // Обновление кэша
        soundCache.forEach((key, cachedSounds) -> {
            cachedSounds.removeIf(cachedSound -> cachedSound.getId().equals(soundId));
            if (cachedSounds.isEmpty()) {
                logger.info("Cache entry for '{}' is now empty and will be removed", key);
                soundCache.remove(key);
            }
        });

        logger.info("Sound with id '{}' deleted and cache updated", soundId);
    }


    private Sound mapSoundFields(Sound sound, Map<String, Object> soundData) {
        if (soundData.containsKey("name")) {
            sound.setName((String) soundData.get("name"));
        }
        if (soundData.containsKey("text")) {
            sound.setText((String) soundData.get("text"));
        }
        if (soundData.containsKey("date")) {
            sound.setDate(Date.valueOf((String) soundData.get("date")));
        }

        // Привязываем альбом, если указан
        if (soundData.containsKey("album")) {
            Map<String, Object> albumData = (Map<String, Object>) soundData.get("album");

            Album album;
            if (albumData.containsKey("id")) {
                Long albumId = ((Number) albumData.get("id")).longValue();
                album = albumRepository.findById(albumId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Album not found with ID: " + albumId));
            } else {
                album = new Album();
                album.setName((String) albumData.get("name"));
                album.setDescription((String) albumData.get("description"));
                album = albumRepository.save(album);
            }

            sound.setAlbum(album);
        }

        // Обновляем список создателей
        if (soundData.containsKey("creators")) {
            List<Map<String, Object>> creatorData = (List<Map<String, Object>>) soundData.get("creators");

            Set<User> creators = creatorData.stream()
                    .map(data -> {
                        Long userId = ((Number) data.get("id")).longValue();
                        return userRepository.findById(userId)
                                .orElseThrow(() ->
                                        new EntityNotFoundException("User not found with ID: " + userId));
                    })
                    .collect(Collectors.toSet());

            sound.setCreators(creators);
        }

        return sound;
    }

    public void deleteSounds() {
        soundRepository.deleteAll();
    }

    public List<SoundDto> getSoundsByUserName(String userName) {
        if (soundCache.containsKey(userName)) {
            logger.info("Cache hit: Data found for user '{}'", userName);
            return soundCache.get(userName).stream()
                    .map(SoundMapper::toDto)
                    .toList();
        }

        logger.info("Cache miss: No data for user '{}'. Fetching from database...", userName);

        List<Sound> sounds = soundRepository.findSoundsByUserNameJPQL(userName);

        if (sounds.isEmpty()) {
            logger.warn("No sounds found in database for user '{}'", userName);
        } else {
            logger.info("Data fetched from database for user '{}'. Adding to cache...", userName);
            soundCache.put(userName, sounds);
        }

        return sounds.stream()
                .map(SoundMapper::toDto)
                .toList();
    }

    public List<SoundDto> getSoundsByAlbumName(String albumName) {
        if (soundCache.containsKey(albumName)) {
            logger.info("Cache hit: Data found for album '{}'", albumName);
            return soundCache.get(albumName).stream()
                    .map(SoundMapper::toDto)
                    .toList();
        }

        logger.info("Cache miss: No data for album '{}'. Fetching from database...", albumName);

        List<Sound> sounds = soundRepository.findSoundsByAlbumNameNative(albumName);

        if (sounds.isEmpty()) {
            logger.warn("No sounds found in database for album '{}'", albumName);
        } else {
            logger.info("Data fetched from database for album '{}'. Adding to cache...", albumName);
            soundCache.put(albumName, sounds);
        }

        return sounds.stream()
                .map(SoundMapper::toDto)
                .toList();
    }
}
