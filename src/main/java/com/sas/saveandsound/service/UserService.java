package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.dto.CreatorDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;
    private final UserService self;

    public UserService(UserRepository userRepository, SoundRepository soundRepository, UserService self) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.self = self;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) throw new UserNotFoundException("No users found.");
        return users.stream()
                .map(u -> Boolean.TRUE.equals(u.getRole()) ? UserMapper.toCreatorDto(u) : UserMapper.toDto(u))
                .toList();
    }

    public UserDto searchUser(long id) {
        User user = Optional.ofNullable(userRepository.findById(id))
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found."));
        return Boolean.TRUE.equals(user.getRole()) ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    public UserDto search(String name) {
        User user = Optional.ofNullable(userRepository.findByName(name))
                .orElseThrow(() -> new UserNotFoundException("User with name '" + name + "' not found."));
        return Boolean.TRUE.equals(user.getRole()) ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByName(userDto.getName()) != null) {
            throw new IllegalArgumentException("User with this name already exists");
        }
        User user = UserMapper.toEntity(userDto);
        if (user == null) throw new IllegalArgumentException("Invalid user data");
        user.setSounds(null);
        User saved = userRepository.save(user);
        return Boolean.TRUE.equals(saved.getRole()) ? UserMapper.toCreatorDto(saved) : UserMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUser(long id, UserDto userDto) {
        User user = Optional.ofNullable(userRepository.findById(id))
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        updateUserFields(user, userDto);
        updateUserSounds(user, userDto);

        User saved = userRepository.save(user);
        return Boolean.TRUE.equals(saved.getRole()) ? UserMapper.toCreatorDto(saved) : UserMapper.toDto(saved);
    }

    @Transactional
    public List<UserDto> updateUsers(List<UserDto> userDtos) {
        if (userDtos == null || userDtos.isEmpty()) return List.of();
        return userDtos.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .map(dto -> {
                    try {
                        return self.updateUser(dto.getId(), dto);
                    } catch (UserNotFoundException | IllegalArgumentException _) {
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .toList();
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("User not found.");
        userRepository.deleteById(id);
    }

    public void deleteUsers() {
        userRepository.deleteAll();
    }

    private void updateUserFields(User user, UserDto userDto) {
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getNickname() != null) user.setNickname(userDto.getNickname());
        user.setRole(userDto.getRole());
    }

    private void updateUserSounds(User user, UserDto userDto) {
        if (!(userDto instanceof CreatorDto creatorDto) || creatorDto.getSounds() == null) return;

        Set<Sound> newSounds = creatorDto.getSounds().stream()
                .map(soundDto -> soundRepository.findById(soundDto.getId()).orElse(null))
                .filter(sound -> sound != null)
                .peek(sound -> {
                    if (sound.getCreators() == null) sound.setCreators(new HashSet<>());
                    sound.getCreators().add(user);
                    soundRepository.save(sound);
                })
                .collect(Collectors.toSet());

        user.setSounds(newSounds);
    }
}
