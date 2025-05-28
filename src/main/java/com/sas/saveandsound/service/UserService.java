package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.dto.CreatorDto;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.UserNotFoundException;
import com.sas.saveandsound.mapper.UserMapper;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.repository.UserRepository;
import com.sas.saveandsound.repository.SoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository; // Add this field

    public UserService(UserRepository userRepository, SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) throw new UserNotFoundException("No users found.");
        // Исправлено: не сравниваем с null, просто проверяем значение
        return users.stream()
            .map(u -> Boolean.TRUE.equals(u.getRole())
                ? UserMapper.toCreatorDto(u)
                : UserMapper.toDto(u))
            .toList();
    }

    public UserDto searchUser(long id) {
        User user = userRepository.findById(id);
        if (user == null) throw new UserNotFoundException("User with ID " + id + " not found.");
        // Исправлено: не сравниваем с null, просто проверяем значение
        return Boolean.TRUE.equals(user.getRole())
            ? UserMapper.toCreatorDto(user)
            : UserMapper.toDto(user);
    }

    public UserDto search(String name) {
        User user = userRepository.findByName(name);
        if (user == null) throw new UserNotFoundException("User with name '" + name + "' not found.");
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByName(userDto.getName()) != null) {
            throw new IllegalArgumentException("User with this name already exists");
        }
        User user = UserMapper.toEntity(userDto);
        if (user == null) throw new IllegalArgumentException("Invalid user data");
        user.setSounds(null); // sounds не обрабатываем
        User saved = userRepository.save(user);
        return saved.getRole() ? UserMapper.toCreatorDto(saved) : UserMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUser(long id, UserDto userDto) {
        User user = userRepository.findById(id);
        if (user == null) throw new UserNotFoundException("User not found with ID: " + id);

        // Обновляем основные поля
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getNickname() != null) user.setNickname(userDto.getNickname());
        user.setRole(userDto.getRole());

        // --- Обновление связи с песнями (как с альбомами: только set) ---
        if (userDto instanceof CreatorDto creatorDto && creatorDto.getSounds() != null) {
            Set<Sound> newSounds = new HashSet<>();
            for (SoundDto soundDto : creatorDto.getSounds()) {
                if (soundDto == null || soundDto.getId() == null) continue;
                Sound sound = soundRepository.findById(soundDto.getId()).orElse(null);
                if (sound != null) {
                    newSounds.add(sound);
                    if (sound.getCreators() == null) sound.setCreators(new HashSet<>());
                    if (!sound.getCreators().contains(user)) {
                        sound.getCreators().add(user);
                        soundRepository.save(sound);
                    }
                }
            }
            user.setSounds(newSounds);
        }

        User saved = userRepository.save(user);
        return saved.getRole() ? UserMapper.toCreatorDto(saved) : UserMapper.toDto(saved);
    }

    @Transactional
    public List<UserDto> updateUsers(List<UserDto> userDtos) {
        if (userDtos == null) return List.of();
        List<UserDto> result = new java.util.ArrayList<>();
        for (UserDto dto : userDtos) {
            if (dto == null || dto.getId() == null) continue;
            try {
                result.add(updateUser(dto.getId(), dto));
            } catch (UserNotFoundException | IllegalArgumentException ex) {
                // skip not found or invalid
            }
        }
        return result;
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("User not found.");
        userRepository.deleteById(id);
    }

    public void deleteUsers() {
        userRepository.deleteAll();
    }
}
