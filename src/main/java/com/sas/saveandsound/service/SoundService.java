package com.sas.saveandsound.service;

import com.sas.saveandsound.cashe.SoundCache;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.repository.AlbumRepository;
import com.sas.saveandsound.repository.SoundRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final AlbumRepository albumRepository;
    private final SoundCache soundCache; // Используем SoundCache вместо Map
    private static final Logger logger = LoggerFactory.getLogger(SoundService.class);

    public SoundService(SoundRepository soundRepository,
                        AlbumRepository albumRepository,
                        SoundCache soundCache) {
        this.soundRepository = soundRepository;
        this.albumRepository = albumRepository;
        this.soundCache = soundCache;
    }

    public List<SoundDto> getAllSounds() {
        logger.info("Fetching all sounds...");
        List<Sound> sounds = soundRepository.findAll();
        if (sounds.isEmpty()) {
            logger.warn("No sounds found.");
            throw new SoundNotFoundException("No sounds found.");
        }
        logger.info("Found {} sounds.", sounds.size());
        return sounds.stream()
                .map(SoundMapper::toDto)
                .toList();
    }

    public SoundDto search(long id) {
        logger.info("Fetching sound with ID: {}", id);
        Sound sound = soundRepository.findById(id);
        if (sound == null) {
            logger.error("Sound with ID {} not found.", id);
            throw new SoundNotFoundException("Sound with ID " + id + " not found.");
        }
        logger.info("Sound with ID {} retrieved successfully.", id);
        return SoundMapper.toDto(sound);
    }

    public List<SoundDto> search(String name) {
        logger.info("Searching sounds by name: {}", name);
        List<Sound> sounds = soundRepository.findByName(name);
        if (sounds.isEmpty()) {
            logger.warn("No sounds found with the name '{}'.", name);
            throw new SoundNotFoundException("No sounds found with the name '" + name + "'.");
        }
        logger.info("Found {} sounds with the name '{}'.", sounds.size(), name);
        return sounds.stream().map(SoundMapper::toDto).toList();
    }

    public List<SoundDto> getSoundsByUserName(String userName) {
        logger.info("Fetching sounds by user name: {}", userName);
        String sanitizedUserName = userName.replaceAll("[\\n\\r\\t]", "_");
        List<Sound> sounds;
        if (soundCache.containsKey(sanitizedUserName)) {
            logger.info("Cache hit: Data found for user '{}'.", sanitizedUserName);
            sounds = soundCache.get(sanitizedUserName);
        } else {
            logger.info("Cache miss: Fetching data from database for user '{}'.", sanitizedUserName);
            sounds = soundRepository.findSoundsByUserNameJPQL(userName);
            if (sounds.isEmpty()) {
                logger.warn("No sounds found for user '{}'.", sanitizedUserName);
                throw new SoundNotFoundException("No sounds found from " + sanitizedUserName + ".");
            }
            soundCache.put(sanitizedUserName, sounds);
        }
        logger.info("Found {} sounds for user '{}'.", sounds.size(), sanitizedUserName);
        return sounds.stream().map(SoundMapper::toDto).toList();
    }

    public List<SoundDto> getSoundsByAlbumName(String albumName) {
        logger.info("Fetching sounds by album name: {}", albumName);
        String sanitizedAlbumName = albumName.replaceAll("[\\n\\r\\t]", "_");
        List<Sound> sounds;
        if (soundCache.containsKey(sanitizedAlbumName)) {
            logger.info("Cache hit: Data found for album '{}'.", sanitizedAlbumName);
            sounds = soundCache.get(sanitizedAlbumName);
        } else {
            logger.info("Cache miss: Fetching data from database for album '{}'.", sanitizedAlbumName);
            sounds = soundRepository.findSoundsByAlbumNameNative(albumName);
            if (sounds.isEmpty()) {
                logger.warn("No sounds found for album '{}'.", sanitizedAlbumName);
                throw new SoundNotFoundException("No sounds found for album " + sanitizedAlbumName + ".");
            }
            soundCache.put(sanitizedAlbumName, sounds);
        }
        logger.info("Found {} sounds for album '{}'.", sounds.size(), sanitizedAlbumName);
        return sounds.stream().map(SoundMapper::toDto).toList();
    }

    @Transactional
    public SoundDto createSound(SoundDto soundDto) {
        logger.info("Creating a new sound: {}", soundDto.getName());
        Sound newSound = mapSoundFields(new Sound(), soundDto);

        if (newSound.getAlbum() != null && newSound.getAlbum().getId() == null) {
            logger.info("Saving new album for sound.");
            Album savedAlbum = albumRepository.save(newSound.getAlbum());
            newSound.setAlbum(savedAlbum);
        }

        Sound savedSound = soundRepository.save(newSound);
        logger.info("Sound created successfully with ID: {}", savedSound.getId());
        return SoundMapper.toDto(savedSound);
    }

    @Transactional
    public SoundDto updateSound(long id, SoundDto soundDto) {
        logger.info("Updating sound with ID: {}", id);
        Sound existingSound = soundRepository.findById(id);
        if (existingSound == null) {
            logger.error("Sound with ID {} not found for update.", id);
            throw new SoundNotFoundException("Sound with ID " + id + " not found.");
        }

        Sound updatedSound = mapSoundFields(existingSound, soundDto);
        logger.info("Fields updated for sound with ID: {}", id);

        if (updatedSound.getAlbum() != null && updatedSound.getAlbum().getId() == null) {
            logger.info("Saving new album for updated sound.");
            Album savedAlbum = albumRepository.save(updatedSound.getAlbum());
            updatedSound.setAlbum(savedAlbum);
        }

        Sound savedUpdatedSound = soundRepository.save(updatedSound);
        logger.info("Sound updated successfully with ID: {}", id);
        return SoundMapper.toDto(savedUpdatedSound);
    }

    public void deleteSound(long soundId) {
        logger.info("Deleting sound with ID: {}", soundId);
        Sound sound = soundRepository.findById(soundId);
        if (sound == null) {
            logger.error("Sound not found with ID: {}", soundId);
            throw new SoundNotFoundException("Unable to delete sound with ID " +
                    soundId + ". Sound not found.");
        }
        soundRepository.delete(sound);
        logger.info("Sound deleted successfully with ID: {}", soundId);
    }

    public void deleteSounds() {
        logger.info("Deleting all sounds...");
        soundRepository.deleteAll();
        logger.info("All sounds deleted successfully.");
    }

    private Sound mapSoundFields(Sound sound, SoundDto soundDto) {
        setSoundName(sound, soundDto);
        sound.setText(soundDto.getText());
        sound.setDate(soundDto.getDate());
        sound.setAlbum(processAlbum(soundDto));
        return sound;
    }

    private void setSoundName(Sound sound, SoundDto soundDto) {
        if (soundDto.getName() != null && !soundDto.getName().trim().isEmpty()) {
            sound.setName(soundDto.getName());
        } else {
            throw new IllegalArgumentException("Sound name cannot be null, empty, or contain only spaces");
        }
    }

    private Album processAlbum(SoundDto soundDto) {
        if (soundDto.getAlbum() == null) {
            return null; // Если альбом не передан, устанавливаем null
        }

        if (soundDto.getAlbum().getId() == null) {
            logger.warn("Album ID is null in SoundDto. Setting album to null.");
            return null; // Если ID равен null, устанавливаем null
        }

        return findAlbumById(soundDto.getAlbum().getId());
    }

    private Album findAlbumById(Long albumId) {
        Optional<Album> optionalAlbum = albumRepository.findById(albumId);
        if (optionalAlbum.isPresent()) {
            return optionalAlbum.get();
        } else {
            throw new IllegalArgumentException("Album with ID " + albumId + " does not exist");
        }
    }




}
