package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.mapper.UserMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;
    private final SoundService soundService;

    public UserService(UserRepository userRepository,
                       SoundRepository soundRepository, SoundService soundService) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.soundService = soundService;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    public UserDto searchUser(long id) {
        User user = userRepository.findById(id);
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    public UserDto search(String name) {
        User user = userRepository.findByName(name);
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
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

    @Transactional
    public UserDto createUser(Map<String, Object> userData) {
        User newUser = mapUserFields(new User(), userData);
        User savedUser = userRepository.save(newUser);
        return savedUser.getRole() ? UserMapper.toCreatorDto(savedUser) : UserMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(long id, Map<String, Object> userData) {
        User existingUser = userRepository.findById(id);

        if (existingUser == null) {
            throw new EntityNotFoundException("User not found");
        }

        mapUserFields(existingUser, userData); // Обновляем поля
        User updatedUser = userRepository.save(existingUser);
        return updatedUser.getRole() ? UserMapper.toCreatorDto(updatedUser) : UserMapper.toDto(updatedUser);
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    // ✅ Вынесенный метод для заполнения/обновления полей User

    private User mapUserFields(User user, Map<String, Object> userData) {
        if (userData.containsKey("name")) {
            user.setName((String) userData.get("name"));
        }
        if (userData.containsKey("email")) {
            user.setEmail((String) userData.get("email"));
        }
        if (userData.containsKey("nickname")) {
            user.setNickname((String) userData.get("nickname"));
        }
        if (userData.containsKey("role")) {
            user.setRole((boolean) userData.get("role"));
        }

        // Если это креатор, обновляем список песен
        if (user.getRole() && userData.containsKey("sounds")) {
            List<Map<String, Object>> soundsData = (List<Map<String, Object>>) userData.get("sounds");

            Set<Sound> sounds = soundsData.stream()
                    .map(data -> {
                        if (data.containsKey("id")) {
                            Long soundId = ((Number) data.get("id")).longValue();
                            return SoundMapper.toEntity(soundService.updateSound(soundId, data));
                        } else {
                            return SoundMapper.toEntity(soundService.createSound(data));
                        }
                    })
                    .collect(Collectors.toSet());

            user.setSounds(sounds);
        }

        return user;
    }

}
