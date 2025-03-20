package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.mapper.UserMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;

    public UserService(UserRepository userRepository, SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
    }

    @Transactional
    public void removeCreatorAndDeleteOrphanSound(Long soundId, Long creatorId) {
        Sound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Sound not found"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        sound.getCreators().remove(creator);
        if (sound.getCreators().isEmpty()) {
            soundRepository.delete(sound);
        } else {
            soundRepository.save(sound);
        }
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    public UserDto search(long id) {
        return UserMapper.toDto(userRepository.findById(id));
    }

    public UserDto search(String name) {
        return UserMapper.toDto(userRepository.findByName(name));
    }

    public UserDto createUser(UserDto userDto) {
        return UserMapper.toDto(userRepository.save(UserMapper.toEntity(userDto)));
    }

    @Transactional
    public UserDto updateUser(long id, UserDto userDto) {
        // Находим существующую песню
        User existingUser = userRepository.findById(id);

        // Обновляем поля сущности
        existingUser.setName(userDto.getName()); // Обновляем имя

        // Обновляем создателей
        if (userDto.getSounds() != null && !userDto.getSounds().isEmpty()) {
            Set<SoundDto> updatedSounds = userDto.getSounds();
            existingUser.setSounds(updatedSounds.stream()
                    .map(SoundMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        // Сохраняем изменения в репозитории
        User updatedUser = userRepository.save(existingUser);

        // Преобразуем сущность обратно в DTO и возвращаем
        return UserMapper.toDto(updatedUser);
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}
