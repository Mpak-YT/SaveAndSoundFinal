package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.CreatorDto;
import com.sas.saveandsound.dto.SoundDto; // Убедитесь, что импортирован
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.exception.UserNotFoundException;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.mapper.UserMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet; // Импорт HashSet
import java.util.List;
import java.util.Objects; // Импорт Objects для filter
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;
    private final SoundService soundService;
    // private final SoundMapper soundMapper; // Если SoundMapper - бин

    private void logUserNotFound(long id) {
        logger.error("User with ID {} not found.", id);
    }

    public UserService(UserRepository userRepository,
                       SoundRepository soundRepository,
                       SoundService soundService) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
        this.soundService = soundService;
    }

    public List<UserDto> getAllUsers() {
        logger.info("Fetching all users...");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            logger.warn("No users found.");
            throw new UserNotFoundException("No users found.");
        }
        logger.info("Found {} users.", users.size());
        // Используем статический маппер
        return users.stream()
                .map(UserMapper::toDto) // или user -> UserMapper.toDto(user)
                .toList();
    }

    public UserDto searchUser(long id) {
        logger.info("Fetching user with ID: {}", id);
        // Используем ваш подход Entity/null
        User user = userRepository.findById(id);
        if (user == null) {
            logUserNotFound(id);
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        logger.info("User with ID {} retrieved successfully.", id);
        // Используем статический маппер
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    public UserDto search(String name) {
        logger.info("Searching for user with name: {}", name);
        // Используем ваш подход Entity/null
        User user = userRepository.findByName(name);
        if (user == null) {
            logger.error("User with name '{}' not found.", name);
            throw new UserNotFoundException("User with name '" + name + "' not found.");
        }
        logger.info("User with name '{}' retrieved successfully.", name);
        // Используем статический маппер
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        logger.info("Creating a new user: {}", userDto.getName());

        User newUser = createMappedUser(userDto);
        if (newUser.getRole() && userDto instanceof CreatorDto creatorDto) {
            handleCreatorSounds(newUser, creatorDto);
        } else {
            newUser.setSounds(new HashSet<>());
        }

        User savedUser = userRepository.save(newUser);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        return mapUserToDto(savedUser);
    }

    private User createMappedUser(UserDto userDto) {
        User newUser = UserMapper.toEntity(userDto);
        if (newUser == null) {
            logger.error("UserMapper.toEntity returned null for input DTO");
            throw new IllegalArgumentException("Input user data is invalid.");
        }
        return newUser;
    }

    private void handleCreatorSounds(User newUser, CreatorDto creatorDto) {
        Set<Sound> sounds = creatorDto.getSounds() != null && !creatorDto.getSounds().isEmpty()
                ? processSounds(newUser, creatorDto.getSounds())
                : new HashSet<>();

        newUser.setSounds(sounds);
    }

    private Set<Sound> processSounds(User newUser, Set<SoundDto> soundDtos) {
        return soundDtos.stream()
                .filter(Objects::nonNull)
                .map(soundDto -> createOrMapSound(newUser, soundDto))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Sound createOrMapSound(User newUser, SoundDto soundDto) {
        Sound sound = null;
        try {
            SoundDto resultDto = soundService.createSound(soundDto);
            if (resultDto != null) {
                sound = SoundMapper.toEntity(resultDto);
                if (sound != null) {
                    sound.setCreators(Set.of(newUser));
                    logger.debug("Successfully created and mapped sound: {}", sound.getId());
                } else {
                    logger.error("Sound entity is null after mapping DTO: {}", resultDto);
                }
            } else {
                logger.warn("SoundService.createSound returned null DTO for: {}", soundDto.getName());
            }
        } catch (Exception e) {
            logger.error("Error creating sound DTO: {}", soundDto, e);
        }
        return sound;
    }

    @Transactional
    public UserDto updateUser(long id, UserDto userDto) {
        logger.info("Updating user with ID: {}", id);

        User existingUser = findExistingUser(id);
        updateBasicFields(existingUser, userDto);
        boolean previousRole = existingUser.getRole();

        processSoundsForCreator(existingUser, userDto);
        clearSoundsIfRoleChanged(existingUser, previousRole);

        User savedUser = userRepository.save(existingUser);
        logger.info("User with ID {} updated successfully.", id);
        return mapUserToDto(savedUser);
    }

    private User findExistingUser(long id) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) {
            logUserNotFound(id);
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        return existingUser;
    }

    private void updateBasicFields(User user, UserDto userDto) {
        user.setName(getUpdatedField(userDto.getName(), user.getName()));
        user.setEmail(getUpdatedField(userDto.getEmail(), user.getEmail()));
        user.setNickname(getUpdatedField(userDto.getNickname(), user.getNickname()));
        user.setRole(userDto.getRole());
    }

    private String getUpdatedField(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }

    private void processSoundsForCreator(User user, UserDto userDto) {
        if (user.getRole() && userDto instanceof CreatorDto creatorDto) {
            if (creatorDto.getSounds() == null) {
                logger.warn("Received CreatorDto with null sounds list for user ID {}. Existing sounds remain unchanged.", user.getId());
                return;
            }

            Set<Sound> processedSounds = creatorDto.getSounds().isEmpty()
                    ? clearSounds(user.getId())
                    : processAndMapSounds(creatorDto.getSounds(), user);

            user.setSounds(processedSounds);
        }
    }

    private Set<Sound> clearSounds(long userId) {
        logger.debug("Received empty sound list for creator update ID {}. Clearing sounds.", userId);
        return new HashSet<>();
    }

    private Set<Sound> processAndMapSounds(Set<SoundDto> soundDtos, User user) {
        return soundDtos.stream()
                .filter(Objects::nonNull)
                .map(soundDto -> processSound(soundDto, user))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Sound processSound(SoundDto soundDto, User user) {
        try {
            SoundDto resultDto = soundDto.getId() != null
                    ? soundService.updateSound(soundDto.getId(), soundDto)
                    : soundService.createSound(soundDto);

            if (resultDto == null) {
                logger.warn("SoundService returned null DTO for input: {}", soundDto);
                return null;
            }

            Sound sound = SoundMapper.toEntity(resultDto);
            if (sound != null) {
                sound.setCreators(Set.of(user));
                logger.debug("Successfully processed sound entity ID: {}", sound.getId());
            } else {
                logger.error("Sound entity is null after mapping result DTO: {}. Skipping sound.", resultDto);
            }
            return sound;
        } catch (Exception e) {
            logger.error("Error processing sound DTO during update: {}", soundDto, e);
            return null;
        }
    }

    private void clearSoundsIfRoleChanged(User user, boolean previousRole) {
        if (!user.getRole() && previousRole) {
            logger.debug("User ID {} role changed from creator to regular. Clearing sounds.", user.getId());
            user.setSounds(new HashSet<>());
        }
    }

    private UserDto mapUserToDto(User user) {
        return user.getRole() ? UserMapper.toCreatorDto(user) : UserMapper.toDto(user);
    }


    @Transactional
    public List<UserDto> updateUsers(List<UserDto> usersDto) {
        logger.info("Bulk updating users...");
        // Используем ваш оригинальный код со stream().map(), но он менее устойчив к ошибкам
        List<UserDto> updatedUsers = usersDto.stream()
                .filter(Objects::nonNull) // Добавим пропуск null DTO в списке
                .map(userDto -> {
                    try {
                        return updateUser(userDto.getId(), userDto);
                    } catch (UserNotFoundException e) {
                        logger.error("User not found during bulk update for ID: {}", userDto.getId());
                        return null; // Возвращаем null при ошибке "не найдено"
                    } catch (Exception e) {
                        logger.error("Unexpected error during bulk update for user ID: {}",
                                userDto.getId(), e);
                        return null; // Возвращаем null при других ошибках
                    }
                })
                .filter(Objects::nonNull) // Убираем null результаты после map
                .toList();
        logger.info("Successfully processed bulk update request. Updated {} users.", updatedUsers.size());
        return updatedUsers;
    }

    // Метод removeCreatorAndDeleteOrphanSound оставлен без изменений (использует Optional)
    @Transactional
    public void removeCreatorAndDeleteOrphanSound(Long soundId, Long creatorId) {
        logger.info("Removing creator with ID {} from sound ID {}.", creatorId, soundId);

        Sound sound = soundRepository.findById(soundId) // Ожидает Optional
                .orElseThrow(() -> {
                    logger.error("Sound with ID {} not found.", soundId);
                    return new SoundNotFoundException("Sound not found with ID: " + soundId);
                });

        User creator = userRepository.findById(creatorId) // Ожидает Optional
                .orElseThrow(() -> {
                    logger.error("Creator with ID {} not found.", creatorId);
                    return new UserNotFoundException("Creator not found with ID: " + creatorId);
                });

        Set<User> creators = sound.getCreators();
        if (creators == null) {
            logger.warn("Creators set for sound ID {} was null. Initializing.", soundId);
            creators = new HashSet<>();
            sound.setCreators(creators);
        }

        boolean removed = creators.remove(creator);
        if (!removed) {
            logger.warn("Creator ID {} not in creators set for sound ID {}.", creatorId, soundId);
        }


        if (creators.isEmpty()) {
            logger.info("Sound with ID {} has no creators after removal, deleting it.", soundId);
            soundRepository.delete(sound);
        } else {
            logger.debug("Sound ID {} still has creators, saving changes.", soundId);
            soundRepository.save(sound);
        }
    }

    // Методы deleteUser и deleteUsers оставлены без изменений
    public void deleteUser(long id) {
        logger.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logUserNotFound(id);
            throw new UserNotFoundException("Unable to delete user with ID: " + id + ". User not found.");
        }
        userRepository.deleteById(id);
        logger.info("User with ID {} deleted successfully.", id);
    }

    public void deleteUsers() {
        logger.info("Deleting all users...");
        userRepository.deleteAll();
        logger.info("All users deleted successfully.");
    }
}