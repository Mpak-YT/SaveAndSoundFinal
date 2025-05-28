package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.repository.AlbumRepository;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import com.sas.saveandsound.exception.DuplicateSoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sas.saveandsound.exception.AlbumNotFoundException;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    public SoundService(SoundRepository soundRepository, UserRepository userRepository,
                        AlbumRepository albumRepository) {
        this.soundRepository = soundRepository;
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
    }

    public List<SoundDto> getAllSounds() {
        List<Sound> sounds = soundRepository.findAll();
        if (sounds.isEmpty()) throw new SoundNotFoundException("No sounds found.");
        return sounds.stream().map(SoundMapper::toDto).toList();
    }

    public SoundDto search(long id) {
        Sound sound = soundRepository.findById(id);
        if (sound == null) {
            throw new SoundNotFoundException("Sound with ID " + id + " not foun");
        }
        return SoundMapper.toDto(sound); // Use static method call
    }

    public List<SoundDto> search(String name) {
        List<Sound> sounds = soundRepository.findByName(name);
        if (sounds.isEmpty()) {
            throw new SoundNotFoundException("No sounds found with the name '" + name + "'.");
        }
        return sounds.stream().map(SoundMapper::toDto).toList();
    }

    public SoundDto createSound(SoundDto soundDto) {
        // Check for duplicate sound name within the same album or without an album
        Optional<Sound> existingSound = soundRepository
            .findByNameAndAlbumId(soundDto.getName(),
                soundDto.getAlbum() != null ? soundDto.getAlbum().getId() : null);

        if (existingSound.isPresent()) {
            throw new DuplicateSoundException("A sound with the name '"
                + soundDto.getName() + "' already exists in this album or without an album.");
        }

        Sound sound = new Sound();
        sound.setName(soundDto.getName());
        sound.setText(soundDto.getText());
        sound.setDate(soundDto.getDate());

        // Handle album association

        // Handle album association
        if (soundDto.getAlbum() != null && soundDto.getAlbum().getId() != null) {
            Album album = albumRepository.findById(soundDto.getAlbum().getId())
                                        .orElseThrow(() -> new AlbumNotFoundException("Album with ID "
                                                + soundDto.getAlbum().getId() + " not foun"));
            sound.setAlbum(album);
            album.getSounds().add(sound); // ensures both sides are updated
        }

        // Handle creators
        if (soundDto.getCreators() != null) {
            for (UserDto creatorDto : soundDto.getCreators()) {
                User creator = userRepository.findById(creatorDto.getId()).orElse(null);
                if (creator == null) throw new IllegalArgumentException("Creator with ID " + creatorDto.getId() +
                        " not found");
                sound.getCreators().add(creator);
                creator.getSounds().add(sound);
            }
        }

        Sound savedSound = soundRepository.save(sound);
        return SoundMapper.toDto(savedSound); // Use static method call
    }

    @Transactional
    public SoundDto updateSound(Long id, SoundDto soundDto) {
        Sound sound = soundRepository.findById(id)
                .orElseThrow(() -> new SoundNotFoundException("Sound with ID " + id + " not found."));

        checkDuplicateSound(id, soundDto);

        if (soundDto.getName() != null) sound.setName(soundDto.getName());
        sound.setText(soundDto.getText());
        sound.setDate(soundDto.getDate());

        Album newAlbum = updateAlbumAssociation(sound, soundDto);
        sound.setAlbum(newAlbum);

        Set<User> updatedCreators = updateCreators(sound, soundDto.getCreators());
        sound.setCreators(updatedCreators);

        Sound savedSound = soundRepository.save(sound);
        return SoundMapper.toDto(savedSound);
    }

    // Проверка дубликатов
    private void checkDuplicateSound(Long id, SoundDto soundDto) {
        Optional<Sound> existingSound = soundRepository.findByNameAndAlbumId(
                soundDto.getName(),
                soundDto.getAlbum() != null ? soundDto.getAlbum().getId() : null
        );

        if (existingSound.isPresent() && !existingSound.get().getId().equals(id)) {
            throw new DuplicateSoundException("A sound with the name '" + soundDto.getName() + "' already exists.");
        }
    }

    // Обновление связи с альбомом
    private Album updateAlbumAssociation(Sound sound, SoundDto soundDto) {
        Album oldAlbum = sound.getAlbum();
        Album newAlbum = null;

        if (soundDto.getAlbum() != null && soundDto.getAlbum().getId() != null) {
            newAlbum = albumRepository.findById(soundDto.getAlbum().getId())
                    .orElseThrow(() -> new AlbumNotFoundException("Album not found."));
        }

        if (oldAlbum != null && !oldAlbum.equals(newAlbum)) {
            oldAlbum.getSounds().remove(sound);
            albumRepository.save(oldAlbum);
        }

        if (newAlbum != null && !newAlbum.equals(oldAlbum)) {
            newAlbum.getSounds().add(sound);
            albumRepository.save(newAlbum);
        }

        return newAlbum;
    }

    // Обновление списка авторов
    private Set<User> updateCreators(Sound sound, Set<UserDto> creatorDtos) {
        Set<User> updatedCreators = new HashSet<>();

        if (creatorDtos != null) {
            for (UserDto creatorDto : creatorDtos) {
                User creator = userRepository.findById(creatorDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Creator not found."));
                updatedCreators.add(creator);
            }
        }

        Set<User> currentCreators = new HashSet<>(sound.getCreators());

        for (User creator : currentCreators) {
            if (!updatedCreators.contains(creator)) {
                creator.getSounds().remove(sound);
                userRepository.save(creator);
            }
        }

        for (User creator : updatedCreators) {
            if (!currentCreators.contains(creator)) {
                creator.getSounds().add(sound);
                userRepository.save(creator);
            }
        }

        return updatedCreators;
    }

    public void deleteSound(long id) {
        Sound sound = soundRepository.findById(id);
        if (sound == null) throw new SoundNotFoundException("Sound not found.");
        soundRepository.delete(sound);
    }

    public void deleteSounds() {
        soundRepository.deleteAll();
    }

    public List<SoundDto> getSoundsByAlbumName(String albumName) {
        List<Sound> sounds = soundRepository.findSoundsByAlbumNameNative(albumName);
        if (sounds == null || sounds.isEmpty()) {
            throw new SoundNotFoundException("No sounds found for album " + albumName + ".");
        }
        return sounds.stream().map(SoundMapper::toDto).toList(); // Use static method call
    }

    public List<SoundDto> getSoundsByUserName(String userName) {
        List<Sound> sounds = soundRepository.findSoundsByUserNameJPQL(userName);
        if (sounds == null || sounds.isEmpty()) {
            throw new SoundNotFoundException("No sounds found from " + userName + ".");
        }
        return sounds.stream().map(SoundMapper::toDto).toList(); // Use static method call
    }
}
