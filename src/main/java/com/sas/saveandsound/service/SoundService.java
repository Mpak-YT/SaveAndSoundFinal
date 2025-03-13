package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.SoundRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final SoundMapper soundMapper;

    public SoundService(SoundRepository soundRepository, SoundMapper soundMapper) {
        this.soundRepository = soundRepository;
        this.soundMapper = soundMapper;
    }

    public List<SoundDto> getAllSounds() {
        return soundRepository.findAll()
                .stream()
                .map(soundMapper::toDto)
                .toList();
    }

    public List<SoundDto> search(String name) {
        return soundRepository.findByName(name)
                .stream()
                .map(soundMapper::toDto)
                .toList();
    }

    public SoundDto search(long id) {
        return soundMapper.toDto(soundRepository.findById(id));
    }

    public SoundDto createSound(SoundDto soundDto) {
        return soundMapper.toDto(soundRepository.save(soundMapper.toEntity(soundDto)));
    }

    @Transactional
    public SoundDto updateSound(long id, SoundDto soundDto) {
        // Находим существующую песню
        Sound existingSound = soundRepository.findById(id);

        // Обновляем поля сущности
        existingSound.setName(soundDto.getName()); // Обновляем имя

        // Обновляем создателей
        if (soundDto.getCreatorIds() != null && !soundDto.getCreatorIds().isEmpty()) {
            Set<User> updatedCreators = soundDto.getCreatorIds().stream()
                    .map(creatorId -> soundMapper.getUserRepository() // Найти пользователя по ID
                            .findById(creatorId)
                            .orElseThrow(() ->
                                new IllegalArgumentException("User with ID " + creatorId + " not found")))
                    .collect(Collectors.toSet());
            existingSound.setCreators(updatedCreators);
        }

        // Сохраняем изменения в репозитории
        Sound updatedSound = soundRepository.save(existingSound);

        // Преобразуем сущность обратно в DTO и возвращаем
        return soundMapper.toDto(updatedSound);
    }

    public void deleteSound(long id) {
        soundRepository.deleteById(id);
    }
}
