package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.AlbumRepository;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

    public SoundService(SoundRepository soundRepository,
                        AlbumRepository albumRepository, UserRepository userRepository) {
        this.soundRepository = soundRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
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

        // Если альбом ещё не сохранён, сохраняем его.
        if (newSound.getAlbum() != null && newSound.getAlbum().getId() == null) {
            Album savedAlbum = albumRepository.save(newSound.getAlbum());
            newSound.setAlbum(savedAlbum);
        }

        // Теперь сохраняем звук
        Sound savedSound = soundRepository.save(newSound);
        return SoundMapper.toDto(savedSound);
    }

    @Transactional
    public SoundDto updateSound(long id, Map<String, Object> soundData) {
        Sound existingSound = soundRepository.findById(id);

        if (existingSound == null) {
            throw new EntityNotFoundException("Sound not found");
        }

        // Обновляем поля звука
        Sound existSound = mapSoundFields(existingSound, soundData);

        // Если альбом ещё не сохранён, сохраняем его.
        if (existSound.getAlbum() != null && existSound.getAlbum().getId() == null) {
            Album savedAlbum = albumRepository.save(existSound.getAlbum());
            existSound.setAlbum(savedAlbum);
        }

        // Теперь сохраняем обновлённый звук
        Sound updatedSound = soundRepository.save(existSound);
        return SoundMapper.toDto(updatedSound);
    }


    public void deleteSound(long id) {
        soundRepository.deleteById(id);
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

        // Привязываем альбом, если он указан
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

        // Обновляем креаторов
        if (soundData.containsKey("creators")) {
            List<Map<String, Object>> creatorData = (List<Map<String, Object>>) soundData.get("creators");

            Set<User> creators = creatorData.stream()
                    .map(data -> {
                        Long userId = ((Number) data.get("id")).longValue(); // Преобразуем ID в Long
                        return userRepository.findById(userId)
                                .orElseThrow(() ->
                                        new EntityNotFoundException("User not found with ID: " + userId));
                    })
                    .collect(Collectors.toSet());

            sound.setCreators(creators);
        }


        return sound;
    }

}
